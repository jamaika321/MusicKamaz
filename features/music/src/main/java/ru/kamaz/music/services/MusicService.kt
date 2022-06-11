package ru.kamaz.music.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.ContentValues.TAG
import android.graphics.Color
import android.hardware.usb.UsbManager
import android.media.AudioAttributes
import android.media.AudioManager
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
import com.eckom.xtlibrary.twproject.music.bean.MusicName
import com.eckom.xtlibrary.twproject.music.bean.Record
import com.eckom.xtlibrary.twproject.music.utils.TWMusic
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.biozzlab.twmanager.domain.interfaces.BluetoothManagerListener
import ru.biozzlab.twmanager.domain.interfaces.MusicManagerListener
import ru.biozzlab.twmanager.managers.BluetoothManager
import ru.biozzlab.twmanager.managers.MusicManager
import ru.biozzlab.twmanager.servers.BluetoothServerConnector
import ru.kamaz.music.cache.db.dao.Playback
import ru.kamaz.music.data.MediaManager
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.receiver.BrReceiver
import ru.kamaz.music.services.music.ShuffleHelper
import ru.kamaz.music.ui.TestWidget
import ru.kamaz.music_api.BaseConstants.ACTION_NEXT
import ru.kamaz.music_api.BaseConstants.ACTION_PREV
import ru.kamaz.music_api.BaseConstants.ACTION_TOGGLE_PAUSE
import ru.kamaz.music_api.BaseConstants.APP_WIDGET_UPDATE
import ru.kamaz.music_api.BaseConstants.EXTRA_APP_WIDGET_NAME
import ru.kamaz.music_api.SourceType
import ru.kamaz.music_api.domain.GetFilesUseCase
import ru.kamaz.music_api.interactor.*
import ru.kamaz.music_api.models.FavoriteSongs
import ru.kamaz.music_api.models.HistorySongs
import ru.kamaz.music_api.models.Track
import ru.sir.core.Either
import ru.sir.core.None
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.extensions.launchOn
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject
import kotlin.collections.ArrayList


class MusicService : Service(), MusicServiceInterface.Service, MediaPlayer.OnCompletionListener,
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
    lateinit var deleteFavoriteMusic: DeleteFavoriteMusic

    @Inject
    lateinit var getFilesUseCase: GetFilesUseCase

    val twManager = BluetoothManager()

    private val twManagerMusic = MusicManager()

    lateinit var device: BluetoothDevice

    private val _isNotAuxConnected = MutableStateFlow(false)
    val isNotAuxConnected = _isNotAuxConnected.asStateFlow()

    private val _isNotConnected = MutableStateFlow(true)
    val isNotConnected = _isNotConnected.asStateFlow()

    private val _isNotUSBConnected = MutableStateFlow(false)
    val isNotUSBConnected = _isNotUSBConnected.asStateFlow()

    private var lifecycleJob = Job()

    private lateinit var lifecycleScope: CoroutineScope

    private var audioManager: AudioManager? = null

    val context: Context? = null

    private val widgettest = TestWidget.instance

    lateinit var myViewModel: MusicServiceInterface.ViewModel

    private var currentTrackPosition = 0

    private var tracks = mutableListOf<Track>()

//    private var usbTracks = mutableListOf<Track>()

    private var files = mutableListOf<File>()

    private var randomTracks = ArrayList<Track>()

    private val binder = MyBinder()

    private var likeTrack = ArrayList<FavoriteSongs>()

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

    private val _btPlaying = MutableStateFlow<Boolean>(false)
    val btPlaying = _btPlaying.asStateFlow()

    private val _buttonStateSaved = MutableStateFlow<Boolean>(false)
    val buttonStateSaved = _buttonStateSaved.asStateFlow()

    private val _rvChange = MutableStateFlow(0)
    val rvChange = _rvChange.asStateFlow()

    private val _isAuxModeOn = MutableStateFlow<Boolean>(false)
    val isAuxModeOn = _isAuxModeOn.asStateFlow()

    private val _isBtModeOn = MutableStateFlow<Boolean>(false)
    val isBtModeOn = _isBtModeOn.asStateFlow()

    private val _isDiskModeOn = MutableStateFlow<Boolean>(false)
    val isDiskModeOn = _isDiskModeOn.asStateFlow()

    private val _isUsbModeOn = MutableStateFlow<Boolean>(false)
    val isUsbModeOn = _isUsbModeOn.asStateFlow()

    private val _isBtPlaying = MutableStateFlow<Boolean>(false)
    val isBtPlaying = _isBtPlaying.asStateFlow()

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


    private val _duration = MutableStateFlow("00:00")
    val duration = _duration.asStateFlow()

    private val _idSong = MutableStateFlow(1)
    val idSong = _idSong.asStateFlow()
    private val _tickFlow = MutableSharedFlow<Unit>(replay = 0)
    val tickFlow: MutableSharedFlow<Unit> = _tickFlow

    private val _maxSeek = MutableStateFlow(0)
    val maxSeek = _maxSeek.asStateFlow()

    private val _isFavorite = MutableStateFlow<Boolean>(false)
    val isFavorite = _isFavorite.asStateFlow()

    private val _btDeviceIsConnecting = MutableStateFlow<Boolean>(false)
    val btDeviceIsConnecting = _btDeviceIsConnecting.asStateFlow()

    private val _musicEmpty = MutableStateFlow<Boolean>(false)
    val musicEmpty = _musicEmpty.asStateFlow()

    private val _isShuffleStatus = MutableStateFlow(false)
    val isShuffleStatus = _isShuffleStatus.asStateFlow()


    fun setShuffleMode() {
        when (isShuffleStatus.value) {
            true -> {
                ShuffleHelper.makeShuffleList(tracks, currentTrackPosition)
                Log.i("setShuffleMode", "setShuffleMode: ")
            }
            false -> {
                updateTracks(mediaManager)
            }
        }
    }


    private val scopeIO = CoroutineScope(Dispatchers.IO + Job())
    private fun getRootFilesFromSource(source: SourceType = SourceType.DEVICE) {
        scopeIO.launch {
            getFilesUseCase.getFilesFromSource(source).let {
                Log.i("UsbfileList", "getRootFilesFromSource $it")
                files.addAll(it)
                withContext(Dispatchers.Main) {

                }
            }
        }
    }


    override fun getMusicName(): StateFlow<String> = title
    override fun getArtistName(): StateFlow<String> = artist
    override fun getRepeat(): StateFlow<Int> = repeatHowNow
    override fun getMusicDuration(): StateFlow<String> = duration
    override fun isFavoriteMusic(): StateFlow<Boolean> = isFavorite
    override fun isShuffleOn(): StateFlow<Boolean> = isShuffleStatus
    override fun changeRv(): StateFlow<Int> = rvChange
    override fun isChangeRv() {
        _rvChange.value = 4
    }

    override fun checkDeviceConnection(): StateFlow<Boolean> = isNotConnected
    override fun checkUSBConnection(): StateFlow<Boolean> = isNotUSBConnected
    override fun checkBTConnection(): StateFlow<Boolean> = isNotConnected
    override fun updateWidget(): StateFlow<Boolean> = isNotConnected
    override fun btModeOn(): StateFlow<Boolean> = isBtModeOn
    override fun auxModeOn(): StateFlow<Boolean> = isAuxModeOn
    override fun diskModeOn(): StateFlow<Boolean> = isDiskModeOn
    override fun usbModeOn(): StateFlow<Boolean> = isUsbModeOn
    override fun usbConnect(): StateFlow<Boolean> = isNotUSBConnected
    override fun howModeNow(): Int = 2
    override fun isPlay(): StateFlow<Boolean> = isPlaying

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

        updateTracks(mediaManager)
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

        isPlaying.launchOn(lifecycleScope) {
            widgettest.updatePlayPauseImg(this, it)
        }

        _musicEmpty.value = tracks.isEmpty()
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
        _isNotUSBConnected.value = isAdded
        if (isAdded) {
            getFilesUseCase.getFiles(path)
            startUsbMode(isAdded)
        } else {
            startDiskMode()
        }
    }

    private fun queryLastMusic() {
//        CoroutineScope(Dispatchers.IO).launch {
//
//            val it = queryLastMusic.run(None())
//
//            it.either({
//
//            }, {
//                (if (it.isEmpty()) initTrack(
//                    tracks[currentTrackPosition],
//                    data.value
//                ) else checkCurrentPosition(it))
//
//            })
//        }
    }

    private fun queryFavoriteMusic() {
        CoroutineScope(Dispatchers.IO).launch {
            val it = queryFavoriteMusic.run(QueryFavoriteMusic.Params(data.value))
            it.either({
            }, {
                (if (it.isEmpty()) Log.i("queryFavoriteMusic", "duration${it}")
                else checkFavoriteMusic(it))
            })
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
    }


    override fun onDeviceDisconnected() {
        _isNotConnected.value = true
        startDiskMode()
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicServiceInterface.Service = this@MusicService
    }

    private fun getAudioManager(): AudioManager {
        if (audioManager == null) {
            audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        }
        return audioManager as AudioManager
    }


    override fun onBind(intent: Intent?): IBinder = binder

    override fun setViewModel(viewModel: MusicServiceInterface.ViewModel) {
        this.myViewModel = viewModel
    }

    enum class SourceEnum(val value: Int) {
        BT(0),
        AUX(1),
        DISK(2),
        USB(3)
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


    fun startBtMode() {
        if (!btModeOn().value) {
            changeSource(1)
            stopMediaPlayer()
            startBtListener()
        }
    }

    fun startDiskMode() {
        if (!isDiskModeOn.value) {
            changeSource(2)
            updateTracks(mediaManager)
            nextTrack(2)
        }
    }



    fun startUsbMode(usbOn: Boolean) {
        if (usbOn) {
            if (!isUsbModeOn.value) {
                changeSource(3)
                updateTracks(mediaManager)
                nextTrack(2)
            }
        } else {
            Toast.makeText(this, "Подключите USB устройство", Toast.LENGTH_SHORT).show()
        }
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
            }
            //BT
            1 -> {
                _isUsbModeOn.tryEmit(false)
                _isDiskModeOn.tryEmit(false)
                _isAuxModeOn.tryEmit(false)
                _isBtModeOn.tryEmit(true)
                this.mode = SourceEnum.BT
            }
            //Disk
            2 -> {
                _isUsbModeOn.tryEmit(false)
                _isBtModeOn.tryEmit(false)
                _isAuxModeOn.tryEmit(false)
                _isDiskModeOn.tryEmit(true)
                this.mode = SourceEnum.DISK
            }
            //USB
            3 -> {
                _isDiskModeOn.tryEmit(false)
                _isBtModeOn.tryEmit(false)
                _isAuxModeOn.tryEmit(false)
                _isUsbModeOn.tryEmit(true)
                this.mode = SourceEnum.USB
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
//        twManagerMusic.close()
    }

    fun startBtListener() {
        onBluetoothMusicDataChanged("Name", "Artist")
        playOrPause()
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
        _isFavorite.value = false
        val currentTrack = track
//        updateTracks(mediaManager)
        _idSong.value = currentTrack.id.toInt()
        updateMusicName(currentTrack.title, currentTrack.artist, currentTrack.duration)
        _data.value = track.data
        Log.i("checkTrackCover", "initTrack: ${track.albumArt}")
        _cover.value = track.albumArt
        mediaPlayer.apply {
            stop()
            reset()
            setDataSource(
                if (data1.isEmpty()) {
                    Log.i("init", "initTrack:$track.data ")
                    track.data
                } else {
                    Log.i("initdata1", "initTrack:$data1 ")
                    data1
                }
            )
            prepare()
            updateSeekBar()
        }
        queryFavoriteMusic()
    }

    private fun stopMediaPlayer() {
        mediaPlayer.stop()
//        tracks.clear()
    }

    private fun checkCurrentPosition(data: String) {
//        var i = 0
//        var q = 0
//        CoroutineScope(Dispatchers.IO).launch {
//            while (i in tracks.indices) {
//                if (tracks[q].data != data) {
//                    q++
//                    i++
//                } else {
//                    Log.i(TAG, "checkCurrentPosition $q$data")
//                    initTrack(tracks[q], data)
//                    break
//                }
//            }
//        }
//        currentTrackPosition = q
    }

    override fun lastSavedState() {
        updateSeekBar()
        twManager.startMonitoring(applicationContext) {
            twManagerMusic.addListener(this)
            twManager.requestConnectionInfo()
        }
    }

    override fun firstOpenTrackFound(track: Track) {
        updateTracks(mediaManager)
        val currentTrack = track
        updateMusicName(currentTrack.title, currentTrack.artist, currentTrack.duration)
    }

    override fun playOrPause() : Boolean {
        when (mode) {
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
        _duration.value = "120"
    }

    override fun getMusicImg(albumID: String) {

    }

    override fun pause() {
        mediaPlayer.pause()
        _isPlaying.value = false
    }

    override fun resume() {
        if (musicEmpty.value) {

        } else {
            mediaPlayer.start()
            _isPlaying.value = true
        }
    }

    override fun checkPosition(position: Int) {
        mediaPlayer.seekTo(position)
    }

    private fun updateSeekBar() {
        val duration = mediaPlayer.duration
        myViewModel.onUpdateSeekBar(duration)
    }


    override fun previousTrack() {
        when (mode) {
            SourceEnum.DISK -> {
                if (tracks.isEmpty()) {

                } else {
                    when (currentTrackPosition - 1) {
                        -1 -> currentTrackPosition = tracks.size -1
                        else -> currentTrackPosition--
                    }

                    when (isPlaying.value) {
                        true -> {
                            initTrack(
                                tracks[currentTrackPosition],
                                tracks[currentTrackPosition].data
                            )
                            resume()
                        }
                        false -> initTrack(
                            tracks[currentTrackPosition],
                            tracks[currentTrackPosition].data
                        )
                    }
                }

            }
            SourceEnum.BT -> {
                twManager.playerPrev()
            }
            SourceEnum.AUX -> TODO()
            SourceEnum.USB -> if (tracks.isEmpty()) {

            } else {
                when (currentTrackPosition - 1) {
                    -1 -> currentTrackPosition = tracks.size -1
                    else -> currentTrackPosition--
                }

                when (isPlaying.value) {
                    true -> {
                        initTrack(
                            tracks[currentTrackPosition],
                            tracks[currentTrackPosition].data
                        )
                        resume()
                    }
                    false -> initTrack(
                        tracks[currentTrackPosition],
                        tracks[currentTrackPosition].data
                    )
                }
            }
        }

    }

    private fun compilationMusic() {
        mediaPlayer.setOnCompletionListener(OnCompletionListener {
            Log.i("isPlayingAutoModeMain", "true${isPlaying.value}")
            if (usbConnectionCheck() && isUsbModeOn.value) {
                nextTrack(1)
            } else if (isBtModeOn.value) {
                nextTrack(1)
            } else if (isDiskModeOn.value) {
                nextTrack(1)
            } else {
                startDiskMode()
            }
        })
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
        }
    }

    private fun repeatModeListener(auto: Int) {
        when (auto) {
            0 -> {
                when (repeatMode) {
                    RepeatMusicEnum.REPEAT_ONE_SONG -> {
                        funRepeatAll()
                    }
                    RepeatMusicEnum.REPEAT_ALL -> {
                        funRepeatAll()
                    }
                }
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
        if (tracks.isEmpty()) {
        } else {
            when (currentTrackPosition == tracks.size - 1) {
                true -> {
                    currentTrackPosition = 0
                }
                false -> currentTrackPosition++
            }
            funPlayOneSong()
        }
    }

    private fun funPlayOneSong() {
        when (isPlaying.value) {
            true -> {
                initTrack(
                    tracks[currentTrackPosition],
                    tracks[currentTrackPosition].data
                )
                resume()
                Log.i("isPlayingWhenPlay", "true${isPlaying.value}")
            }
            false -> {
                initTrack(
                    tracks[currentTrackPosition],
                    tracks[currentTrackPosition].data
                )
                Log.i("isPlayingWhenStop", "false${isPlaying.value}")
            }
        }

    }

    private fun restartPlaylist() {
        if (tracks.isEmpty()) {
            when (usbConnectionCheck()) {
                true -> startUsbMode(true)
                false -> clearTrackData()
            }
        } else {
            currentTrackPosition = 0
            nextTrack(1)
        }
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

    override fun updateTracks(mediaManager: MediaManager) {
        when (_isUsbModeOn.value) {
            true -> {
                val result = mediaManager.scanTracks(0)
                if (result is Either.Right) {
                    replaceAllTracks(result.r)
                } else {
                    tracks.clear()
                    _musicEmpty.value = true
                }
            }
            false -> {
                val result = mediaManager.scanMediaFiles()
                if (result is Either.Right) {
                    replaceAllTracks(result.r)
                } else {
                    tracks.clear()
                    _musicEmpty.value = true
                }
            }
        }
        if(isShuffleStatus.value){
            setShuffleMode()
        }

    }

    private fun replaceAllTracks(trackList: List<Track>) {
        tracks.clear()
        tracks.addAll(trackList)
        _musicEmpty.value = tracks.isEmpty()
    }

    fun updateUsbTracks() {
        when (_isUsbModeOn.value) {
            true -> {
                val result = getFilesUseCase.getFiles("/storage/usbdisk0")
                files.addAll(result)
                Log.i("result", "updateUsbTracks: $result")
            }
            false -> {

            }
        }
    }

    fun shuffle() {
        if (isShuffleStatus.value) {
            Log.i("updateTracks", "updateTracks: ${tracks[0]} ")
            tracks = tracks.shuffled().toMutableList()
            Log.i("updateTracks", "updateTracks: ${tracks[0]} ")
        }
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
                _isNotUSBConnected.value = usbConnectionCheck()
                if (_isNotUSBConnected.value) {
                    startUsbMode(true)
                } else {
                    startUsbMode(false)
                }
            }
        }
    }

    override fun usbConnectionCheck(): Boolean {
        _isNotUSBConnected.value = mediaManager.scanTracks(1).isRight
        return mediaManager.scanTracks(1).isRight
    }

    fun getToastConnectBtDevice(btDevise: Boolean) {
        _btDeviceIsConnecting.value = btDevise
        _btDeviceIsConnecting.value = !btDevise
    }

    override fun insertFavoriteMusic() {
        queryFavoriteMusic()
        if (isFavorite.value) {
            deleteFavoriteMusic()
        } else {
            _isFavorite.value = true
            val music = FavoriteSongs(idSong.value, data.value, title.value, artist.value)
            insertFavoriteMusic(InsertFavoriteMusic.Params(music))
        }
    }

    override fun shuffleStatusChange() {
        _isShuffleStatus.value = !isShuffleStatus.value
        setShuffleMode()
        //  shuffle()
    }

    override fun deleteFavoriteMusic() {
        _isFavorite.value = false
        val music = FavoriteSongs(idSong.value, data.value, title.value, artist.value)
        deleteFavoriteMusic(DeleteFavoriteMusic.Params(music))
    }

    fun checkFavoriteMusic(data: String) {
        var i = 0
        CoroutineScope(Dispatchers.IO).launch {
            while (i in tracks.indices) {
                if (tracks[i].data != data) {
                    i++
                } else {
                    _isFavorite.value = true
                    break
                }
            }
        }
    }

    val twMusic: TWMusic = TWMusic.open()
    val usbManager: UsbManager? = null

    override fun insertLastMusic() {
        val music = HistorySongs(
            18,
            idSong.value,
            title.value,
            228,
            1,
            1,
            data.value,
            1,
            1,
            title.value,
            1,
            artist.value,
            artist.value,
            1
        )
        insertLastMusic(InsertLastMusic.Params(music))
    }

    override fun onCompletion(mp: MediaPlayer?) {

    }

    override fun onPlayerPlayPauseState(isPlaying: Boolean) {
        Log.i("btTest", "onPlayerPlayPauseState: $isPlaying")
        when (isPlaying) {
            true -> _isPlaying.value = true
            false -> _isPlaying.value = false
        }
        if (!isBtModeOn.value){
            _isPlaying.value = true
        }
    }


}