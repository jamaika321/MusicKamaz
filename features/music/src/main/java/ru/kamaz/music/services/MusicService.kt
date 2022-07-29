package ru.kamaz.music.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.ContentValues.TAG
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
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.lifecycle.viewModelScope
import com.eckom.xtlibrary.twproject.music.bean.MusicName
import com.eckom.xtlibrary.twproject.music.bean.Record
import com.eckom.xtlibrary.twproject.music.utils.TWMusic
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.biozzlab.twmanager.domain.interfaces.BluetoothManagerListener
import ru.biozzlab.twmanager.domain.interfaces.MusicManagerListener
import ru.biozzlab.twmanager.managers.BluetoothManager
import ru.biozzlab.twmanager.managers.MusicManager
import ru.kamaz.music.cache.db.dao.Playback
import ru.kamaz.music.data.MediaManager
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.receiver.BrReceiver
import ru.kamaz.music.ui.TestWidget
import ru.kamaz.music_api.BaseConstants.ACTION_NEXT
import ru.kamaz.music_api.BaseConstants.ACTION_PREV
import ru.kamaz.music_api.BaseConstants.ACTION_TOGGLE_PAUSE
import ru.kamaz.music_api.BaseConstants.APP_WIDGET_UPDATE
import ru.kamaz.music_api.BaseConstants.EXTRA_APP_WIDGET_NAME
import ru.kamaz.music_api.domain.GetFilesUseCase
import ru.kamaz.music_api.interactor.*
import ru.kamaz.music_api.models.*
import ru.sir.core.Either
import ru.sir.core.None
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.extensions.launchOn
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject


class MusicService : Service(), MusicServiceInterface.
Service, OnCompletionListener,
    MusicManagerListener, BluetoothManagerListener {

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    @Inject
    lateinit var mediaManager: MediaManager

    @Inject
    lateinit var getMusicCover: GetMusicCover

    @Inject
    lateinit var getMusicPosition: GetMusicPosition

    @Inject
    lateinit var insertFavoriteMusic: InsertFavoriteMusic

    @Inject
    lateinit var insertLastMusic: InsertLastMusic

    @Inject
    lateinit var queryLastMusic: QueryLastMusic

    @Inject
    lateinit var queryFavoriteMusic: QueryFavoriteMusic

    @Inject
    lateinit var getAllFavoriteSongs: FavoriteMusicRV

    @Inject
    lateinit var deleteFavoriteMusic: DeleteFavoriteMusic

    @Inject
    lateinit var getFilesUseCase: GetFilesUseCase

    @Inject
    lateinit var rvPlayList: PlayListRV

    @Inject
    lateinit var rvAllFolderWithMusic: AllFolderWithMusicRV

    private val twManager = BluetoothManager()

    private val twManagerMusic = MusicManager()

    private val _isNotAuxConnected = MutableStateFlow(false)
    val isNotAuxConnected = _isNotAuxConnected.asStateFlow()

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

    private var allTracks = MutableStateFlow<List<Track>>(emptyList())

    private var playLists = MutableStateFlow<List<PlayListModel>>(emptyList())

    private var foldersList = MutableStateFlow<List<AllFolderWithMusic>>(emptyList())

    private var files = mutableListOf<File>()

    private var randomTracks = mutableListOf<Track>()

    private val binder = MyBinder()

    private var mode = SourceEnum.DISK

    private var repeatMode = RepeatMusicEnum.REPEAT_ALL

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
            Log.i("recever", "onReceive: recever")
            val action = intent.action
            val cmd = intent.getStringExtra("cmd")
            if (CMDPREV == cmd || ACTIONPREV == action) {
                Log.i("recever", "onReceive: recevernextTrack")
                nextTrack(1)
            } else if (CMDNEXT == cmd || ACTIONNEXT == action) {
                Log.i("recever", "onReceive: recevernextTrack")
                nextTrack(1)
            } else if (CMDPP == cmd || ACTIONPP == action) {
                Log.i("recever", "onReceive: receverplayOrPause")
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

    private val _idSong = MutableStateFlow(1)
    val idSong = _idSong.asStateFlow()

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


    private fun setShuffleMode() {
        when (isShuffleStatus.value) {
            true -> {
                tracks.shuffle()
            }
            false -> {
                replaceAllTracks(emptyList())
            }
        }
    }

    override fun getAllTracks() = allTracks.asStateFlow()
    override fun getPlayLists() = playLists.asStateFlow()
    override fun getFoldersList() = foldersList.asStateFlow()
    override fun playListModeOn() = playListMode.asStateFlow()

    override fun getMusicName(): StateFlow<String> = title
    override fun getArtistName(): StateFlow<String> = artist
    override fun getRepeat(): StateFlow<Int> = repeatHowNow
    override fun getMusicDuration(): StateFlow<Int> = duration
    override fun isFavoriteMusic(): StateFlow<Boolean> = isFavorite
    override fun isShuffleOn(): StateFlow<Boolean> = isShuffleStatus
    override fun getMusicData(): StateFlow<String> = data
    override fun getSource(): StateFlow<String> = sourceName

    override fun checkDeviceConnection(): StateFlow<Boolean> = isNotConnected
    override fun checkUSBConnection(): StateFlow<Boolean> = isUSBConnected
    override fun updateWidget(): StateFlow<Boolean> = isNotConnected
    override fun btModeOn(): StateFlow<Boolean> = isBtModeOn

    override fun auxModeOn(): StateFlow<Boolean> = isAuxModeOn
    override fun diskModeOn(): StateFlow<Boolean> = isDiskModeOn
    override fun usbModeOn(): StateFlow<Boolean> = isUsbModeOn
    override fun howModeNow(): Int = 2
    override fun isPlay(): StateFlow<Boolean> = isPlaying
    override fun lastMusic(): StateFlow<String> = lastMusic


    override fun dialogFragment(): StateFlow<Boolean> = btDeviceIsConnecting
    override fun musicEmpty(): StateFlow<Boolean> = musicEmpty
    override fun coverId(): StateFlow<String> = cover

    override fun init() {
        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setAudioAttributes(audioAttributes)
    }

    val WHERE_MY_CAT_ACTION = "ru.kamaz.musickamaz"
    override fun onCreate() {
        super.onCreate()
        (application as BaseApplication).getComponent<MusicComponent>().inject(this)
        init()
        registerReceiver(widgetIntentReceiver, IntentFilter(APP_WIDGET_UPDATE))
        initMediaPlayer()
        startForeground()
        startMusicListener()
        initLifecycleScope()


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

        artist.launchOn(lifecycleScope) {
            widgettest.updateTestArtist(this, it)
        }

        title.launchOn(lifecycleScope) {
            widgettest.updateTestTitle(this, it)
        }

        duration.launchOn(lifecycleScope) {
            widgettest.updateTestDuration(this, it)
        }

        isPlaying.launchOn(lifecycleScope) {
            widgettest.updatePlayPauseImg(this, it)
        }

        compilationMusic()
        // queryLastMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribeLifecycleScope()
//        stopMusicListener()
    }

    override fun onSdStatusChanged(path: String, isAdded: Boolean) {
    }

    override fun onUsbStatusChanged(path: String, isAdded: Boolean) {
        Log.i("USBstatus", "onUsbStatusChanged:$path ")
        _isUSBConnected.value = isAdded
        updateTracks("all")
        if (isAdded && isUSBConnected.value) {
            startUsbMode()
        } else {
            mediaPlayer.stop()
            startDiskMode()
        }
    }

    private fun getFavoriteMusicList() {
        CoroutineScope(Dispatchers.IO).launch {
            getAllFavoriteSongs.run(None()).collect {
                changeFavoriteStatus(it)
            }
        }
    }

    var mCList: Record? = null

    var mUSBRecordArrayList: MutableList<Record> = mutableListOf()

    fun addRecordUSB(path: String) {
        for (r in mUSBRecordArrayList) {
            if (path == r.mName) {
                return
            }
        }
        val r = Record(path, 2, 0)
        loadVolume(r, path)
        if (r.mLength > 0) {
            mUSBRecordArrayList.add(r)
        }
        if (mCList != null && mCList?.mName == "USB") {
            mCList = mUSBRecordArrayList.get(0)
        }
    }

    fun loadVolume(record: Record?, volume: String?) {
        if (record != null && volume != null) {
            try {
                var br: BufferedReader? = null
                try {
                    var xpath: String? = null
                    xpath = if (TWMusic.mSDKINTis4) {
                        if (volume.startsWith("/mnt/extsd")) {
                            "/data/tw/" + volume.substring(5)
                        } else if (volume.startsWith("/mnt/usbhost/Storage")) {
                            "/data/tw/" + volume.substring(13)
                        } else {
                            "$volume/DCIM"
                        }
                    } else {
                        if (volume.startsWith("/storage/usb") || volume.startsWith("/storage/extsd")) {
                            "/data/tw/" + volume.substring(9)
                        } else {
                            "$volume/DCIM"
                        }
                    }
                    br = BufferedReader(FileReader("$xpath/.music"))
                    var path: String? = null
                    val l = java.util.ArrayList<MusicName>()
                    while (br.readLine().also { path = it } != null) {
                        val f = File("$volume/$path")
                        if (f.canRead() && f.isDirectory) {
                            val n = f.name
                            val p = f.absolutePath
                            if (n == ".") {
                                val p2 = p.substring(0, p.lastIndexOf("/"))
                                val p3 = p2.substring(p2.lastIndexOf("/") + 1)
                                l.add(MusicName(p3, p))
                            } else {
                                l.add(MusicName(n, p))
                            }
                        }
                    }
                    record.setLength(l.size)
                    for (n in l) {
                        record.add(n)
                    }
                    l.clear()
                } catch (e: java.lang.Exception) {

                } finally {
                    if (br != null) {
                        br.close()
                        br = null
                    }
                }
            } catch (e: java.lang.Exception) {

            }
        }
    }

    override fun onDeviceConnected() {
        _isNotConnected.value = false
        Log.i("DeviceConnection", "Устройство подключено")
    }


    override fun onDeviceDisconnected() {
        _isNotConnected.value = true
        Log.i("DeviceConnection", "Устройство отключено")
//        startDiskMode()
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicServiceInterface.Service {
            Log.i("ReviewTest_Binder", "MyBinder")
            return this@MusicService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i("ReviewTest_Binder", "onBind: ")
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
        PLAYLIST(4)
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
            _title.value = name
            _artist.value = artist
        }
    }

    fun startAuxMode() {
        changeSource(0)
        _isAuxModeOn.tryEmit(true)
        Toast.makeText(this, "В разработке.", Toast.LENGTH_LONG).show()
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
            changeSource(2)
            if (allTracks.value.isEmpty()) {
                updateTracks("5")
            } else {
                replaceAllTracks(emptyList())
            }
            _sourceName.value = "Disk"
            nextTrack(2)
        }
    }


    private fun startUsbMode() {
        if (!isUsbModeOn.value && isUSBConnected.value) {
            changeSource(3)
            if (allTracks.value.isEmpty()) {
                updateTracks("5")
            } else {
                replaceAllTracks(emptyList())
            }
            _sourceName.value = "USB"
            nextTrack(2)
        }
    }

    private fun startPlayListMode() {
        changeSource(4)
    }

    private fun changeSource(sourceEnum: Int) {
        if (btModeOn().value) {
            stopBtListener()
            startMusicListener()
        }
        when (sourceEnum) {
            //AUX
            0 -> {
                _isUsbModeOn.tryEmit(false)
                _isDiskModeOn.tryEmit(false)
                _isBtModeOn.tryEmit(false)
                _isPlaylistModeOn.tryEmit(false)
            }
            //BT
            1 -> {
                _isUsbModeOn.tryEmit(false)
                _isDiskModeOn.tryEmit(false)
                _isAuxModeOn.tryEmit(false)
                _isPlaylistModeOn.tryEmit(false)
                _isBtModeOn.tryEmit(true)
                this.mode = SourceEnum.BT
                playListMode.value = "bt"
            }
            //Disk
            2 -> {
                _isUsbModeOn.tryEmit(false)
                _isBtModeOn.tryEmit(false)
                _isAuxModeOn.tryEmit(false)
                _isPlaylistModeOn.tryEmit(false)
                _isDiskModeOn.tryEmit(true)
                this.mode = SourceEnum.DISK
                playListMode.value = "disk"
            }
            //USB
            3 -> {
                _isDiskModeOn.tryEmit(false)
                _isBtModeOn.tryEmit(false)
                _isAuxModeOn.tryEmit(false)
                _isPlaylistModeOn.tryEmit(false)
                _isUsbModeOn.tryEmit(true)
                this.mode = SourceEnum.USB
                playListMode.value = "usb"
            }
            //PlayLists
            4 -> {
                _isDiskModeOn.tryEmit(false)
                _isBtModeOn.tryEmit(false)
                _isAuxModeOn.tryEmit(false)
                _isUsbModeOn.tryEmit(false)
                _isPlaylistModeOn.tryEmit(true)
                this.mode = SourceEnum.PLAYLIST
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
        Log.i("ReviewTest_Closed", "appClosed: ")
        stopMediaPlayer()
        twManager.stopMonitoring(applicationContext)
        mediaManager.deleteAlbumArtDir()
        System.exit(1)
//        twManagerMusic.close()
    }

    fun startBtListener() {
        onBluetoothMusicDataChanged("Name", "Artist")
        playOrPause()
        _cover.value = ""
    }

    fun startMusicListener() {
        twManagerMusic.addListener(this)
//        twManager.addListener(this)
    }


    fun stopMusicListener() {
        mediaPlayer.stop()
        twManagerMusic.close()
        twManagerMusic.removeListener(this)
    }

    override fun clearTrackData() {
        updateMusicName("", "", 120)
        _idSong.value = 1
        _data.value = ""
    }


    override fun initTrack(track: Track, data1: String) {
        tracks.find { it.data == track.data }.let {
            if (it != null) _currentTrackPosition.value = it.id.toInt()
            else _currentTrackPosition.value = 0
        }
        val currentTrack = track
        _isFavorite.value = track.favorite
        _lastMusic.value = currentTrack.data
        _idSong.value = currentTrack.id.toInt()
        updateMusicName(currentTrack.title, currentTrack.artist, currentTrack.duration)
        _data.value = track.data
        _duration.value = track.duration.toInt()
        if (track.albumArt != "") {
            _cover.value = track.albumArt
        } else {
            _cover.value = ""
        }
        mediaPlayer.apply {
            stop()
            reset()
            setDataSource(
                if (data1.isEmpty()) {
                    track.data
                } else {
                    data1
                }
            )
//            setOnErrorListener { mediaPlayer, i, i2 ->
//                _isPlaylistModeOn.value = false
//                return@setOnErrorListener false
//            }
            prepare()
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer.stop()
//        tracks.clear()
    }

    override fun lastSavedState() {
        twManager.startMonitoring(applicationContext) {
            twManagerMusic.addListener(this)
            twManager.requestConnectionInfo()
        }
    }

//    override fun firstOpenTrackFound(track: Track) {
//        updateTracks()
//        val currentTrack = track
//        updateMusicName(currentTrack.title, currentTrack.artist, currentTrack.duration)
//    }

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


    private fun updateMusicName(title: String, artist: String, duration: Long) {
        _title.value = title
        _artist.value = artist
//        _duration.value = duration.toInt()
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

    private fun prevTrackHelper(){
        if (!tracks.isEmpty()) {
            when (currentTrackPosition.value - 1) {
                -1 -> _currentTrackPosition.value = tracks.size - 1
                else -> _currentTrackPosition.value--
            }

            when (isPlaying.value) {
                true -> {
                    initTrack(
                        tracks[currentTrackPosition.value],
                        tracks[currentTrackPosition.value].data
                    )
                    resume()
                }
                false -> initTrack(
                    tracks[currentTrackPosition.value],
                    tracks[currentTrackPosition.value].data
                )
            }
        }
    }

    private fun compilationMusic() {
        mediaPlayer.setOnCompletionListener {
            Log.i("isPlayingAutoModeMain", "true${isPlaying.value}")
            checkUsb()
            if (isUSBConnected.value && isUsbModeOn.value) {
                nextTrack(1)
            } else if (isBtModeOn.value) {
                nextTrack(1)
            } else if (isDiskModeOn.value) {
                nextTrack(1)


            } else if (isPlaylistModeOn.value) {
                nextTrack(1)
            } else {
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
        Log.i("ReviewTest", "nextTrack: $auto")
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

    private fun funRepeatAll() {
        if (tracks.isNotEmpty()) {
            when (currentTrackPosition.value == tracks.size - 1) {
                true -> {
                    _currentTrackPosition.value = 0
                }
                false -> _currentTrackPosition.value++
            }
            funPlayOneSong()
        }
    }

    private fun funPlayOneSong() {
        when (isPlaying.value) {
            true -> {
                initTrack(
                    tracks[currentTrackPosition.value],
                    tracks[currentTrackPosition.value].data
                )
                resume()
            }
            false -> {
                initTrack(
                    tracks[currentTrackPosition.value],
                    tracks[currentTrackPosition.value].data
                )
            }
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
    var playback: Playback? = null

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

        if (intent != null) {
            when (intent.action) {
                ACTION_TOGGLE_PAUSE -> playOrPause()
                ACTION_NEXT -> nextTrack(0)
                ACTION_PREV -> previousTrack()
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
            .setPriority(PRIORITY_MIN)
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

    override fun updateTracks(loadMode: String) {
        val result = mediaManager.getMediaFilesFromPath("all", loadMode)
        if (result is Either.Right) {
            replaceAllTracks(result.r)
            _musicEmpty.value = false
        } else {
            _musicEmpty.value = true
        }
        if (loadMode == "5") {
            loadTracksOnCoroutine("all")
            loadPlayLists()
        }
        if (isShuffleStatus.value) {
            setShuffleMode()
        }
        rvAllFolderWithMusic(None()) { it.either({}, ::fillFoldersList) }
    }

    private fun loadTracksOnCoroutine(loadMode: String) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        val job = scope.launch {
            updateTracks(loadMode)
            val result = async {
                getFavoriteMusicList()
            }.await()
        }
        job.start()
    }

    private fun replaceAllTracks(trackList: List<Track>, source: String = "") {
        if (trackList.isNotEmpty()) {
            allTracks.value = trackList
        }
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
                    _sourceName.value = "All"
                    fillTracks(id.toLong(), it)
                    id++
                }
            }
        }
        if (tracks.isEmpty()) {
            _musicEmpty.value = tracks.isEmpty()
            when (mode) {
                SourceEnum.DISK -> {
                    startUsbMode()
                }
                SourceEnum.USB -> {
                    _isUSBConnected.value = false
                    startDiskMode()
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

    override fun initPlayListSource(track: Track, playList: PlayListSource) {
        sourceSelection(SourceEnum.PLAYLIST)
        playListMode.value = playList.type
        when (playList.type) {
            "all" -> replaceAllTracks(emptyList())
            "folder" -> getFolderTracks(playList)
            "playList" -> getPlayListTracks(playList)
            "favorite" -> getFavoritePlayList(playList)
        }
    }

    private fun getFolderTracks(folder: PlayListSource) {
        tracks.clear()
        _sourceName.value = folder.name
        var id = 0
        foldersList.value.forEach { data ->
            if (data.dir == folder.name)
                allTracks.value.forEach {
                    if (it.data.contains(
                            data.data + File.separator + it.title.replace(" ", "_")
                                .replace(",", "_").replace("-", "_").replace("'", "_")
                        )
                    ) {
                        fillTracks(id.toLong(), it)
                        id++
                    }
                }
        }
    }

    private fun getPlayListTracks(playList: PlayListSource) {
        tracks.clear()
        _sourceName.value = playList.name
        var id = 0
        playLists.value.find { it.title == playList.name }?.trackDataList?.forEach { track ->
            allTracks.value.find { it.data == track }.let {
                if (it != null) {
                    fillTracks(id.toLong(), it)
                    id++
                }
            }
        }
    }

    private fun getFavoritePlayList(playList: PlayListSource) {
        tracks.clear()
        _sourceName.value = playList.name
        var id = 0
        allTracks.value.forEach {
            Log.i("ReviewTest_favorite", " : ${it.title} = ${it.favorite.toString()} ")
            if (it.favorite) {
                fillTracks(id.toLong(), it)
                id++
            }
        }
    }

    private fun loadPlayLists() {
        CoroutineScope(Dispatchers.IO).launch {
            rvPlayList.run(None()).collect {
                playLists.value = it
            }
        }
    }

    private fun loadFolderList() {

    }

    private fun fillFoldersList(allFoldersList: List<AllFolderWithMusic>) {
        foldersList.value = allFoldersList
    }

    override fun intMediaPlayer() {
        Log.d(TAG, "init")
        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setAudioAttributes(audioAttributes)
    }

    override fun sourceSelection(action: SourceEnum) {
        when (action) {
            SourceEnum.AUX -> {
                if (isNotAuxConnected.value) {
                } else {
                }
            }
            SourceEnum.BT -> {
                if (isNotConnected.value) {
                    getToastConnectBtDevice(true)
                } else {
                    startBtMode()
                }
            }
            SourceEnum.DISK -> startDiskMode()
            SourceEnum.USB -> {
                checkUsb()
                if (_isUSBConnected.value) {
                    startUsbMode()
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
        setShuffleMode()
    }

    override fun insertFavoriteMusic(data: String) {
        allTracks.value.forEach {
            if (it.data == data) {
                if (it.favorite) {
                    deleteFavoriteMusic(it)
                } else {
                    insertFavoriteToDB(it)
                }
                it.favorite = !it.favorite
                _isFavorite.value = !isFavorite.value
            }
        }
    }

    private fun insertFavoriteToDB(track: Track) {
        insertFavoriteMusic(InsertFavoriteMusic.Params(track))
    }

    private fun deleteFavoriteMusic(track: Track) {
        deleteFavoriteMusic(DeleteFavoriteMusic.Params(track))
    }

    private fun changeFavoriteStatus(list: List<Track>) {
        CoroutineScope(Dispatchers.IO).launch {
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
    }

    override fun insertLastMusic() {
        val music = HistorySongs(
            18,
            idSong.value,
            title.value,
            228,
            1,
            duration.value.toLong(),
            data.value,
            1,
            cover.value,
            title.value,
            1,
            artist.value,
            artist.value,
            1
        )
        insertLastMusic(InsertLastMusic.Params(music))
    }

    override fun onPlayerPlayPauseState(isPlaying: Boolean) {
        Log.i("btTest", "onPlayerPlayPauseState: $isPlaying")
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


}