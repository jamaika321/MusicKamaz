package ru.kamaz.music.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.biozzlab.twmanager.domain.interfaces.BluetoothManagerListener
import ru.biozzlab.twmanager.domain.interfaces.MusicManagerListener
import ru.biozzlab.twmanager.managers.BluetoothManager
import ru.biozzlab.twmanager.managers.MusicManager
import ru.kamaz.music.data.MediaManager
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.domain.TestSettings
import ru.kamaz.music.intentlauncher.managers.TileMusicManager
import ru.kamaz.music.intentlauncher.receivers.TileListenerReceiver
import ru.kamaz.music.receiver.BrReceiver
import ru.kamaz.music.ui.TestWidget
import ru.kamaz.music_api.BaseConstants.APP_WIDGET_UPDATE
import ru.kamaz.music_api.BaseConstants.EXTRA_APP_WIDGET_NAME
import ru.kamaz.music_api.interactor.*
import ru.kamaz.music_api.models.*
import ru.sir.core.Either
import ru.sir.core.None
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.extensions.launchOn
import java.io.File
import javax.inject.Inject


class MusicService : Service(), MusicServiceInterface.
Service, OnCompletionListener,
    MusicManagerListener, BluetoothManagerListener {

    private lateinit var tileMusicManager: TileMusicManager

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    @Inject
    lateinit var mediaManager: MediaManager

    @Inject
    lateinit var insertFavoriteMusic: InsertFavoriteMusic

    @Inject
    lateinit var testSettings: TestSettings

    @Inject
    lateinit var insertLastMusic: InsertLastMusic

    @Inject
    lateinit var queryLastMusic: QueryLastMusic

    @Inject
    lateinit var getAllFavoriteSongs: FavoriteMusicRV

    @Inject
    lateinit var deleteFavoriteMusic: DeleteFavoriteMusic

    @Inject
    lateinit var rvPlayList: PlayListRV

    @Inject
    lateinit var rvAllFolderWithMusic: AllFolderWithMusicRV

    @Inject
    lateinit var getMusicPosition: GetMusicPosition

    private val twManager = BluetoothManager()

    private val twManagerMusic = MusicManager()

    private val _isNotConnected = MutableStateFlow(true)
    val isNotConnected = _isNotConnected.asStateFlow()

    private val _isUSBConnected = MutableStateFlow(false)
    val isUSBConnected = _isUSBConnected.asStateFlow()

    private var lifecycleJob = Job()

    private lateinit var lifecycleScope: CoroutineScope

    val context: Context? = null

    private val widgettest = TestWidget.instance

    lateinit var myViewModel: MusicServiceInterface.ViewModel

    private var tracks = mutableListOf<Track>()

    //Все треки
    private var allTracks = MutableStateFlow<List<Track>>(emptyList())

    //Плейлисты
    private var playLists = MutableStateFlow<List<PlayListModel>>(emptyList())

    //Папки
    private var foldersList = MutableStateFlow<List<AllFolderWithMusic>>(emptyList())

    //Избранное
    private var favoriteTracks = MutableStateFlow<List<Track>>(emptyList())

    private var files = mutableListOf<File>()

    private val binder = MyBinder()

    private var mode = SourceEnum.DISK

    private var repeatMode = RepeatMusicEnum.REPEAT_ALL

    private val emptyTrack = Track(
        0L, "Unknown", "Unknown", "",
        0, "Unknown", "",
        source = "source"
    )

    private val _cover = MutableStateFlow("")
    val cover = _cover.asStateFlow()

    private val _title = MutableStateFlow("Unknown")
    val title = _title.asStateFlow()

    private val _artist = MutableStateFlow("Unknown")
    val artist = _artist.asStateFlow()

    private val _repeatHowNow = MutableStateFlow(2)
    val repeatHowNow = _repeatHowNow.asStateFlow()

    private val _data = MutableStateFlow("")
    val data = _data.asStateFlow()

    private val _isPlaying = MutableStateFlow<Boolean>(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isAuxModeOn = MutableStateFlow<Boolean>(false)
    val isAuxModeOn = _isAuxModeOn.asStateFlow()

    private val _isBtModeOn = MutableStateFlow<Boolean>(false)
    val isBtModeOn = _isBtModeOn.asStateFlow()

    private val playListMode = MutableStateFlow("")

    private val _isDiskModeOn = MutableStateFlow<Boolean>(false)
    val isDiskModeOn = _isDiskModeOn.asStateFlow()

    private val _isUsbModeOn = MutableStateFlow<Boolean>(false)
    val isUsbModeOn = _isUsbModeOn.asStateFlow()

    private val _isPlaylistModeOn = MutableStateFlow<Boolean>(false)
    val isPlaylistModeOn = _isPlaylistModeOn.asStateFlow()

    private val _isDefaultModeOn = MutableStateFlow(false)
    val isDefaultModeOn = _isDefaultModeOn.asStateFlow()

    private var trackInfo = MutableStateFlow(emptyTrack)

    val br: BroadcastReceiver = BrReceiver()
    var CMDPREV = "prev"
    var CMDNEXT = "next"
    var CMDPP = "pp"
    var CMDUPDATE = "update"
    var ACTIONCMD = "com.tw.music.action.cmd"
    var ACTIONPREV = "com.tw.music.action.prev"
    var ACTIONNEXT = "com.tw.music.action.next"
    var ACTIONPP = "com.tw.music.action.pp"


    private val mIntentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("Test_Receiver", "onReceive: recever")
            val action = intent.action
            val cmd = intent.getStringExtra("cmd")
            if (CMDPREV == cmd || ACTIONPREV == action) {
                nextTrack(1)
            } else if (CMDNEXT == cmd || ACTIONNEXT == action) {
                nextTrack(1)
            } else if (CMDPP == cmd || ACTIONPP == action) {
                playOrPause()
            } else if (CMDUPDATE == cmd) {
                val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                //mWidget.performUpdate(this@MusicService, appWidgetIds)
            }
        }
    }

    private val _currentTrackPosition = MutableStateFlow(0)
    val currentTrackPosition = _currentTrackPosition.asStateFlow()

    private val _sourceName = MutableStateFlow("")
    val sourceName = _sourceName.asStateFlow()


    private val _duration = MutableStateFlow(0)
    val duration = _duration.asStateFlow()

    private val _lastMusic = MutableStateFlow("")
    val lastMusic = _lastMusic.asStateFlow()

    private val _isFavorite = MutableStateFlow<Boolean>(false)
    val isFavorite = _isFavorite.asStateFlow()

    private val _btDeviceIsConnecting = MutableStateFlow<Boolean>(false)
    val btDeviceIsConnecting = _btDeviceIsConnecting.asStateFlow()

    private val _musicEmpty = MutableStateFlow<Boolean>(false)
    val musicEmpty = _musicEmpty.asStateFlow()

    private val _isShuffleStatus = MutableStateFlow(false)
    val isShuffleStatus = _isShuffleStatus.asStateFlow()

    override fun getAllTracks() = allTracks.asStateFlow()
    override fun getPlayLists() = playLists.asStateFlow()
    override fun getFoldersList() = foldersList.asStateFlow()
    override fun getTrackInfo(): StateFlow<Track> = trackInfo
    override fun getRepeat(): StateFlow<Int> = repeatHowNow
    override fun isShuffleOn(): StateFlow<Boolean> = isShuffleStatus
    override fun getSource(): StateFlow<String> = sourceName
    override fun checkUSBConnection(): StateFlow<Boolean> = isUSBConnected
    override fun btModeOn(): StateFlow<Boolean> = isBtModeOn
    override fun defaultModeOn(): StateFlow<Boolean> = isDefaultModeOn
    override fun auxModeOn(): StateFlow<Boolean> = isAuxModeOn
    override fun diskModeOn(): StateFlow<Boolean> = isDiskModeOn
    override fun usbModeOn(): StateFlow<Boolean> = isUsbModeOn
    override fun playListModeOn() = playListMode.asStateFlow()
    override fun howModeNow(): Int = 2
    override fun isPlay(): StateFlow<Boolean> = isPlaying
    override fun lastMusic(): StateFlow<String> = lastMusic
    override fun dialogFragment(): StateFlow<Boolean> = btDeviceIsConnecting
    override fun musicEmpty(): StateFlow<Boolean> = musicEmpty
//    override fun coverId(): StateFlow<String> = cover
//    override fun getMusicName(): StateFlow<String> = title
//    override fun getArtistName(): StateFlow<String> = artist
//    override fun getMusicDuration(): StateFlow<Int> = duration
    override fun isFavoriteMusic(): StateFlow<Boolean> = isFavorite
//    override fun getMusicData(): StateFlow<String> = data

    override fun init() {
        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer.setOnCompletionListener(this)//Слушатель завершения воспроизведения потока(одной песни)
        mediaPlayer.setAudioAttributes(audioAttributes)
    }

    val WHERE_MY_CAT_ACTION = "ru.kamaz.musickamaz"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        (application as BaseApplication).getComponent<MusicComponent>().inject(this)
        init()
        registerReceiver(widgetIntentReceiver, IntentFilter(APP_WIDGET_UPDATE))
        initMediaPlayer()
        startForeground()
        startMusicListener()
        initLifecycleScope()

        tileMusicManager = TileMusicManager(applicationContext)
        TileListenerReceiver.prevMusic = { previousTrack() }
        TileListenerReceiver.nextMusic = { nextTrack(1) }
        TileListenerReceiver.playPauseMusic = { playOrPause() }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
            addAction(ACTIONPP)
            addAction(ACTIONCMD)
            addAction(ACTIONPREV)
            addAction(ACTIONNEXT)
            addAction(WHERE_MY_CAT_ACTION)
        }
        registerReceiver(br, filter)

        twManager.startMonitoring(applicationContext) {
            twManager.addListener(this)
            twManager.requestConnectionInfo()
        }

        getMusicPosition()
            .stateIn(lifecycleScope, SharingStarted.Lazily, 0)
            .launchOn(lifecycleScope) {
                tileMusicManager.sendDuration(duration.value)
                tileMusicManager.sendProgress(it)
                tileMusicManager.sendArtist(artist.value)
                tileMusicManager.sendTitle(title.value)
                tileMusicManager.sendIsPlaying(isPlaying.value)
            }

        cover.launchOn(lifecycleScope) {
            widgettest.updateTestImage(this, it)
            tileMusicManager.sendAlbumImagePath(it)
        }

        artist.launchOn(lifecycleScope) {
            widgettest.updateTestArtist(this, it)
        }

        title.launchOn(lifecycleScope) {
            widgettest.updateTestTitle(this, it)
        }

        duration.launchOn(lifecycleScope) {
//            widgettest.updateTestDuration(this, it)
        }

        isPlaying.launchOn(lifecycleScope) {
            widgettest.updatePlayPauseImg(this, it)
        }

        completionListener()
        loadLastSavedMusic(666)
        changeSourceFromEQButton()
    }

    private fun loadLastSavedMusic(id: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val loading = withContext(Dispatchers.IO) {
                queryLastMusic.run(QueryLastMusic.Params(id))
            }
            if (loading is Either.Right && loading.r.isNotEmpty()) {
                loadAllLists()
                loading.r[0].let { track ->
                    val _loading = mediaManager.loadLastTrack(listOf(track.data))
                    if (_loading is Either.Right) {
                        tracks = _loading.r
                        initTrack(tracks.find { it.data == track.data } ?: emptyTrack,
                            track.data)
                        when (track.source) {
                            "disk" -> {
                                startDiskMode()
                            }
                            "usb" -> {
                                startUsbMode()
                            }
                            else -> {
                                initPlayListSource(PlayListSource(track.source, track.sourceName))
                                resume()
                            }
                        }
                    } else {
                        defaultLoading(id)
                    }
                }
            } else {
                defaultLoading(id)
            }
        }
    }

    private fun defaultLoading(id: Int) {
        when (id) {
            666 -> {
                updateTracks("all", "5")
                startDiskMode()
            }
            777 -> {
                updateTracks("sdCard", "5")
                startUsbMode()
            }
        }
        if (tracks.size != 0) nextTrack(2)
    }

    private fun loadAllLists() {
        CoroutineScope(Dispatchers.IO).launch {
            loadTracksOnCoroutine("5")
            delay(10000)
            //TODO
            getAllFavoriteSongs.run(None()).collect {
                favoriteTracks.value = it
                changeFavoriteStatus(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribeLifecycleScope()
        TileListenerReceiver.prevMusic = {  }
        TileListenerReceiver.nextMusic = {  }
        TileListenerReceiver.playPauseMusic = {  }
    }

    override fun onSdStatusChanged(path: String, isAdded: Boolean) {
    }

    override fun onUsbStatusChanged(path: String, isAdded: Boolean) {
        Log.i("Test_UsbPath", " $path ")
        checkUsb()
        if (isUSBConnected.value && !isUsbModeOn.value) {
            loadLastSavedMusic(777)
        }
    }

    override fun onDeviceConnected() {
        _isNotConnected.value = false
    }


    override fun onDeviceDisconnected() {
        _isNotConnected.value = true
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicServiceInterface.Service {
            return this@MusicService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun setViewModel(viewModel: MusicServiceInterface.ViewModel) {
        this.myViewModel = viewModel
    }

    enum class SourceEnum(val value: Int) {
        BT(0),
        AUX(1),
        DISK(2),
        USB(3),
        PLAYLIST(4),
        DEFAULT(5)
    }

    enum class RepeatMusicEnum(val value: Int) {
        REPEAT_ONE_SONG(1),
        REPEAT_ALL(2)
    }

    override fun changeRepeatMode() {
        when (repeatHowNow.value) {
            1 -> _repeatHowNow.value = 2
            2 -> _repeatHowNow.value = 1
        }
        howRepeatMode()
    }

    private fun howRepeatMode() {
        when (repeatHowNow.value) {
            1 -> oneSongRepeat()
            2 -> allSongsRepeat()
        }
    }

    private fun oneSongRepeat() {
        repeatMode = RepeatMusicEnum.REPEAT_ONE_SONG
    }

    private fun allSongsRepeat() {
        repeatMode = RepeatMusicEnum.REPEAT_ALL
    }

    override fun onBluetoothMusicDataChanged(name: String, artist: String) {
        if (isBtModeOn.value) {
            trackInfo.value = Track(
                0L, name, artist, "",
                0, "Unknown", "",
                source = "source"
            )
            _title.value = name
            _artist.value = artist
        }
    }

    private fun startAuxMode() {
        changeSource(0)
        testSettings.onCreate()
        stopMediaPlayer()
//        twManagerMusic.requestSource(false)
    }


    private fun startBtMode() {
        if (!isBtModeOn.value) {
            changeSource(1)
            stopMediaPlayer()
            startBtListener()
            _sourceName.value = ""
        }
    }

    private fun startDiskMode() {
        if (!isDiskModeOn.value) {
            Log.i("Test_StartDisk", "tracks.size: ${tracks.size}")
            changeSource(2)
            if (allTracks.value.isNotEmpty()) {
                replaceAllTracks(emptyList(), false)
            }
            resume()
        }
    }

    private fun startUsbMode() {
        Log.i("Test_StartUsb", "tracyBundy")
        if (!isUsbModeOn.value) {
            Log.i("Test_StartUsb", "tracks.size: ${tracks.size}")
            changeSource(3)
            if (allTracks.value.isNotEmpty()) {
                replaceAllTracks(emptyList(), false)
            }
            initTrack(tracks[0], tracks[0].data)
            resume()
        }
    }

    private fun startPlayListMode() {
        changeSource(4)
    }

    private fun startDefaultMode() {
        changeSource(5)
    }

    private fun changeSource(sourceEnum: Int) {
        if (btModeOn().value) {
            stopBtListener()
            startMusicListener()
        }
        if (auxModeOn().value) {
            testSettings.onPause()
            twManagerMusic.requestSource(true)
        }
        _isUsbModeOn.tryEmit(false)
        _isDiskModeOn.tryEmit(false)
        _isAuxModeOn.tryEmit(false)
        _isBtModeOn.tryEmit(false)
        _isPlaylistModeOn.tryEmit(false)
        _isDefaultModeOn.tryEmit(false)

        tileMusicManager.sendSourceMusicType(sourceEnum)

        when (sourceEnum) {
            //AUX
            0 -> {
                _isAuxModeOn.tryEmit(true)
                this.mode = SourceEnum.AUX
            }
            //BT
            1 -> {
                _isBtModeOn.tryEmit(true)
                this.mode = SourceEnum.BT
                playListMode.value = "bt"
            }
            //Disk
            2 -> {
                _isDiskModeOn.tryEmit(true)
                this.mode = SourceEnum.DISK
                playListMode.value = "disk"
            }
            //USB
            3 -> {
                _isUsbModeOn.tryEmit(true)
                this.mode = SourceEnum.USB
                playListMode.value = "usb"
            }
            //PlayLists
            4 -> {
                _isPlaylistModeOn.tryEmit(true)
                this.mode = SourceEnum.PLAYLIST
            }
            //Default
            5 -> {
                _isDefaultModeOn.tryEmit(true)
                this.mode = SourceEnum.DEFAULT
            }
        }
    }


    private fun stopBtListener() {
        try {
            if (isPlaying.value) {
                playOrPause()
            }
            twManagerMusic.removeListener(this)
//            twManager.stopMonitoring(applicationContext)
        } catch (e: Exception) {

        }

    }

    override fun appClosed() {
        stopMediaPlayer()
        twManager.stopMonitoring(applicationContext)
        mediaManager.deleteAlbumArtDir()
        System.exit(1)
//        twManagerMusic.close()
    }

    private fun startBtListener() {
        onBluetoothMusicDataChanged("Name", "Artist")
        playOrPause()
        _cover.value = ""
    }

    private fun startMusicListener() {
        twManagerMusic.addListener(this)
//        twManager.addListener(this)
    }


    fun stopMusicListener() {
        mediaPlayer.stop()
        twManagerMusic.close()
        twManagerMusic.removeListener(this)
    }

    override fun initTrack(track: Track, data1: String) {
        Log.i("Test_Init", "  : init ${track.title} ")
        tracks.find { it.data == track.data }.let {
            if (it != null) _currentTrackPosition.value = it.id.toInt()
            else _currentTrackPosition.value = 0
        }
        trackInfo.value = track
//        _isFavorite.value = track.favorite
        _lastMusic.value = track.data
        _title.value = track.title
        _artist.value = track.artist
        _data.value = track.data
        _duration.value = track.duration.toInt()
        _cover.value = track.albumArt
        when (track.source) {
            "usb" -> {
                checkUsb()
                if (!isUSBConnected.value) {
                    tracks.find { it.source == "disk" }.let { if (it == null) startDiskMode() }
                    nextTrack(1)
                } else {
                    mediaPlayer.apply {
                        stop()
                        reset()
                        try {
                            setDataSource(
                                data1.ifEmpty {
                                    track.data
                                }
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            prepare()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            "disk" -> {
                mediaPlayer.apply {
                    stop()
                    reset()
                    try {
                        setDataSource(
                            data1.ifEmpty {
                                track.data
                            }
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        prepare()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            "source" -> {
                mediaPlayer.stop()
            }
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer.stop()
    }

    override fun lastSavedState() {
        twManager.startMonitoring(applicationContext) {
            twManagerMusic.addListener(this)
            twManager.requestConnectionInfo()
        }
        //TODO
        //BT теряет поток когда метод срабатывает
//        twManagerMusic.requestSource(true)

    }

    override fun playOrPause(): Boolean {
        when (mode) {
            SourceEnum.PLAYLIST -> {
                when (isPlaying.value) {
                    true -> pause()
                    false -> resume()
                }
            }
            SourceEnum.DISK -> {
                when (isPlaying.value) {
                    true -> pause()
                    false -> resume()
                }
            }
            SourceEnum.USB -> {
                when (isPlaying.value) {
                    true -> pause()
                    false -> resume()
                }
            }
            SourceEnum.BT -> {
                twManager.playerPlayPause()
            }
            SourceEnum.AUX -> {

            }
        }
        return isPlaying.value
    }

    override fun pause() {
        mediaPlayer.pause()
        _isPlaying.value = false
    }

    override fun resume() {
        if (!musicEmpty.value) {
            mediaPlayer.start()
            _isPlaying.value = true
        }
    }

    override fun checkPosition(position: Int) {
        mediaPlayer.seekTo(position)
    }

    override fun previousTrack() {
        when (mode) {
            SourceEnum.DISK -> prevTrackHelper()
            SourceEnum.BT -> twManager.playerPrev()
            SourceEnum.AUX -> TODO()
            SourceEnum.USB -> prevTrackHelper()
            SourceEnum.PLAYLIST -> prevTrackHelper()
        }
    }

    private fun prevTrackHelper() {
        if (tracks.isNotEmpty()) {
            if (!isShuffleStatus.value) {
                when (currentTrackPosition.value - 1) {
                    -1 -> {
                        if (mediaPlayer.currentPosition < 5000) {
                            _currentTrackPosition.value = tracks.size - 1
                        }
                    }
                    else -> {
                        if (mediaPlayer.currentPosition < 5000) {
                            _currentTrackPosition.value--
                        }
                    }
                }
            } else {
                _currentTrackPosition.value = (0 until tracks.size).random()
            }
            initTrack(
                tracks[currentTrackPosition.value],
                tracks[currentTrackPosition.value].data
            )
            if (isPlaying.value) resume()
        }
    }

    private fun completionListener() {
        mediaPlayer.setOnCompletionListener {
            checkUsb()
            if (isUSBConnected.value && isUsbModeOn.value ||
                isBtModeOn.value || isDiskModeOn.value ||
                isPlaylistModeOn.value || isAuxModeOn.value
            ) {
                nextTrack(1)
            } else {
                _currentTrackPosition.value = 0
                Log.i("Test_Compilation", " : ${isPlaying.value} ")
                startDiskMode()
            }
        }
    }

    override fun checkUsb() {
        _isUSBConnected.value = mediaManager.getMediaFilesFromPath("sdCard", "one").isRight
    }

    private fun initMediaPlayer() {
        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setAudioAttributes(audioAttributes)
    }

    override fun nextTrack(auto: Int) {
        when (mode) {
            SourceEnum.DISK -> {
                repeatModeListener(auto)
            }
            SourceEnum.BT -> {
                twManager.playerNext()
            }
            SourceEnum.AUX -> {
                TODO()
            }
            SourceEnum.USB -> {
                repeatModeListener(auto)
            }
            SourceEnum.PLAYLIST -> {
                repeatModeListener(auto)
            }
        }
    }

    private fun repeatModeListener(auto: Int) {
        if (!musicEmpty.value) {
            when (auto) {
                0 -> {
                    funRepeatAll()
                }
                1 -> {
                    when (repeatMode) {
                        RepeatMusicEnum.REPEAT_ONE_SONG -> {
                            funPlayOneSong()
                        }
                        RepeatMusicEnum.REPEAT_ALL -> {
                            funRepeatAll()
                        }
                    }
                }
                2 -> restartPlaylist()
            }
        }
    }

    private fun funRepeatAll() {
        if (tracks.isNotEmpty()) {
            if (!isShuffleStatus.value)
                when (currentTrackPosition.value == tracks.size - 1) {
                    true -> _currentTrackPosition.value = 0
                    false -> _currentTrackPosition.value++
                } else {
                _currentTrackPosition.value = (0 until tracks.size).random()
            }
            funPlayOneSong()
        }
    }

    private fun funPlayOneSong() {
        initTrack(
            tracks[currentTrackPosition.value],
            tracks[currentTrackPosition.value].data
        )
        if (isPlaying.value) {
            resume()
        }
    }

    private fun restartPlaylist() {
        if (tracks.size - 1 <= currentTrackPosition.value) {
            _currentTrackPosition.value = 0
        }
        funPlayOneSong()
    }

    private val widgetIntentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val command = intent.getStringExtra(EXTRA_APP_WIDGET_NAME)
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            if (command != null) {

            }
        }
    }

    private fun initLifecycleScope() {
        unsubscribeLifecycleScope()
        lifecycleJob = Job()
        lifecycleScope = CoroutineScope(Dispatchers.Main + lifecycleJob)
    }

    fun unsubscribeLifecycleScope() {
        lifecycleJob.apply {
            cancelChildren()
            cancel()
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            val cmd = intent.getStringExtra("cmd")
            if (CMDPREV == cmd || ACTIONPREV == action) {
                previousTrack()
            } else if (CMDNEXT == cmd || ACTIONNEXT == action) {
                nextTrack(0)
            } else if (CMDPP == cmd || ACTIONPP == action) {
                playOrPause()
            }
        }
        return START_STICKY
    }

    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {

                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder
            .setOngoing(true)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setPriority(PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun updateTracks(path: String, loadMode: String) {
        val result = mediaManager.getMediaFilesFromPath(path, loadMode)
        if (result is Either.Right && result.r.isNotEmpty()) {
            replaceAllTracks(result.r, true)
            Log.i("Test_Update", " allTracks.size: ${result.r.size} ")
            _musicEmpty.value = false
        } else {
            _musicEmpty.value = true
            startDefaultMode()
        }
        if (loadMode == "5") {
            loadTracksOnCoroutine("all")
        }
    }

    private fun loadTracksOnCoroutine(loadMode: String) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        val job = scope.launch {
            updateTracks("all", loadMode)
        }
        job.start()
    }

    override fun replaceAllTracks(trackList: List<Track>, replace: Boolean) {
        if (replace) {
            allTracks.value = trackList
        } else {
            tracks.clear()
            var id = 0
            allTracks.value.forEach {
                when (mode) {
                    SourceEnum.DISK -> {
                        if (it.source == "disk") {
                            fillTracks(id.toLong(), it)
                            id++
                        }
                    }
                    SourceEnum.USB -> {
                        if (it.source == "usb") {
                            fillTracks(id.toLong(), it)
                            id++
                        }
                    }
                    SourceEnum.PLAYLIST -> {
                        when (playListMode.value) {
                            "folder" -> getFolderTracks()
                            "playList" -> getPlayListTracks()
                            "favorite" -> getFavoritePlayList()
                            "artists" -> getArtistPlayList()
                            "albums" -> getArtistPlayList()
                            else -> {
                                fillTracks(id.toLong(), it)
                                id++
                            }
                        }
                    }
                }
            }
            if (tracks.isEmpty()) {
                when (mode) {
                    SourceEnum.DISK -> {
                        startUsbMode()
                        Toast.makeText(this, "Файлы не найдены.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        _isUSBConnected.value = false
                        startDiskMode()
                    }
                }
            } else {
                tracks.find { it.data == data.value }.let {
                    if (it != null) {
                        _currentTrackPosition.value = it.id.toInt()
                    }
                }
                CoroutineScope(Dispatchers.IO).launch {
                    rvAllFolderWithMusic(None()) { it.either({}, ::fillFoldersList) }
                    rvPlayList.run(None()).collect { playLists.value = it }
                }
            }
        }
    }

    private fun fillTracks(id: Long, it: Track) {
        tracks.add(
            (Track(
                id,
                it.title,
                it.artist,
                it.data,
                it.duration,
                it.album,
                it.albumArt,
                it.playing,
                it.favorite,
                it.source
            ))
        )
    }

    override fun initPlayListSource(playList: PlayListSource) {
        sourceSelection(SourceEnum.PLAYLIST)
        playListMode.value = playList.type
        selectPlayListMode(playList)
    }

    private fun selectPlayListMode(playList: PlayListSource) {
        _sourceName.value = playList.name
        replaceAllTracks(emptyList(), false)
    }

    private fun getArtistPlayList() {
        var id = 0
        allTracks.value.forEach {
            when (playListMode.value) {
                "artists" -> {
                    if (it.artist == _sourceName.value) {
                        fillTracks(id.toLong(), it)
                        id++
                    }
                }
                "albums" -> {
                    if (it.album == _sourceName.value) {
                        fillTracks(id.toLong(), it)
                        id++
                    }
                }
            }
        }
    }

    private fun getFolderTracks() {
        var id = 0
        foldersList.value.forEach { data ->
            if (data.dir == _sourceName.value)
                allTracks.value.forEach {
                    if (it.data.contains(
                            data.data + File.separator
                        )
                    ) {
                        fillTracks(id.toLong(), it)
                        id++
                    }
                }
        }
    }

    private fun getPlayListTracks() {
        var id = 0
        playLists.value.find { it.title == _sourceName.value }?.trackDataList?.forEach { track ->
            allTracks.value.find { it.data == track }.let {
                if (it != null) {
                    fillTracks(id.toLong(), it)
                    id++
                }
            }
        }
    }

    private fun getFavoritePlayList() {
        var id = 0
        allTracks.value.forEach {
            if (it.favorite) {
                fillTracks(id.toLong(), it)
                id++
            }
        }
    }

    private fun fillFoldersList(allFoldersList: List<AllFolderWithMusic>) {
        foldersList.value = allFoldersList
    }

    override fun sourceSelection(action: SourceEnum) {
        when (action) {
            SourceEnum.AUX -> {
                startAuxMode()
            }
            SourceEnum.BT -> {
                if (!isNotConnected.value) {
                    startBtMode()
                } else {
                    getToastConnectBtDevice(true)
                }
            }
            SourceEnum.DISK -> {
                startDiskMode()
                nextTrack(2)
            }
            SourceEnum.USB -> {
                checkUsb()
                if (isUSBConnected.value) {
                    onUsbStatusChanged("", true)
                } else {
                    Toast.makeText(this, "Файлы не найдены.", Toast.LENGTH_SHORT).show()
                }
            }
            SourceEnum.PLAYLIST -> {
                startPlayListMode()
            }
        }
    }

    fun getToastConnectBtDevice(btDevise: Boolean) {
        _btDeviceIsConnecting.value = btDevise
        _btDeviceIsConnecting.value = !btDevise
    }

    override fun shuffleStatusChange() {
        _isShuffleStatus.value = !isShuffleStatus.value
    }

    override fun insertFavoriteMusic(data: String) {
        allTracks.value.find { it.data == data }.let {
            if (it != null) {
                if (it.favorite) {
                    deleteFavoriteMusic(it)
                    it.favorite = false
                } else {
                    insertFavoriteToDB(it)
                    it.favorite = true
                }
                trackInfo.value.favorite = !trackInfo.value.favorite
                _isFavorite.value = !isFavorite.value
            }
        }
        allTracks.value.forEach {
            if (it.data == data) {
                if (it.favorite) {
                    deleteFavoriteMusic(it)
                } else {
                    insertFavoriteToDB(it)
                }
                it.favorite = !it.favorite
            }
        }
    }

    private fun insertFavoriteToDB(track: Track) {
        insertFavoriteMusic(InsertFavoriteMusic.Params(track))
    }

    private fun deleteFavoriteMusic(track: Track) {
        deleteFavoriteMusic(DeleteFavoriteMusic.Params(track))
        allTracks.value.find { it.data == track.data }.let {
            if (it != null) it.favorite = false
        }
        tracks.find { it.data == track.data }.let {
            if (it != null) it.favorite = false
        }
    }

    private fun changeFavoriteStatus(list: List<Track>) {
        tracks.forEach { track ->
            list.forEach { list ->
                if (track.title == list.title && track.data == list.data) {
                    track.favorite = true
                }
            }
        }
        allTracks.value.forEach { track ->
            list.forEach { list ->
                if (track.data == list.data) {
                    track.favorite = true
                }
            }
        }
    }

    override fun fillSelectedTrack() {

    }

    override fun insertLastMusic(id: Int) {
        val music = HistorySongs(
            id,
            trackInfo.value.data,
            mediaPlayer.currentPosition.toLong(),
            playListMode.value,
            sourceName.value,
            trackInfo.value.favorite
        )
        CoroutineScope(Dispatchers.IO).launch {
            insertLastMusic(InsertLastMusic.Params(music))
        }
    }

    override fun onPlayerPlayPauseState(isPlaying: Boolean) {
        when (isPlaying) {
            true -> _isPlaying.value = true
            false -> _isPlaying.value = false
        }
        if (!isBtModeOn.value) {
            _isPlaying.value = true
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {

    }

    private fun changeSourceFromEQButton(){
        testSettings.start {
            when (it) {
                66 -> {
                    when (mode) {
                        SourceEnum.AUX -> sourceSelection(SourceEnum.BT)
                        SourceEnum.BT -> sourceSelection(SourceEnum.DISK)
                        SourceEnum.DISK -> {
                            checkUsb()
                            if (isUSBConnected.value) sourceSelection(SourceEnum.USB)
//                            else //TODO
                        }
//                        SourceEnum.USB -> //TODO
                    }
                }
            }
        }
    }


}