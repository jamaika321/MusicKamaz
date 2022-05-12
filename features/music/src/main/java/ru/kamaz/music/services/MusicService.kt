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
    MusicManagerListener, BluetoothManagerListener{

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

    private var repeatMode = RepeatMusicEnum.REPEAT_OFF

    private val _cover = MutableStateFlow("")
    val cover = _cover.asStateFlow()

    private val _title = MutableStateFlow("Unknown")
    val title = _title.asStateFlow()

    private val _artist = MutableStateFlow("Unknown")
    val artist = _artist.asStateFlow()

    private val _repeatHowNow = MutableStateFlow(0)
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

    val _isShuffleStatus = MutableStateFlow(false)
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

//    override fun onPlayPauseKeyPressed(isPlaying: Boolean) {
//        Toast.makeText(this, "btPlayPause", Toast.LENGTH_SHORT).show()
//        Log.i("btTest", "onPlayPauseKeyPressed: $isPlaying")
//        super.onPlayPauseKeyPressed()
//    }


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

    override fun dialogFragment(): StateFlow<Boolean> = btDeviceIsConnecting
    override fun musicEmpty(): StateFlow<Boolean> = musicEmpty

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
            }else{
                startDiskMode(isAdded)
            }
    }

    private fun queryLastMusic() {
        CoroutineScope(Dispatchers.IO).launch {

            val it = queryLastMusic.run(None())

            it.either({

            }, {
                (if (it.isEmpty()) initTrack(
                    tracks[currentTrackPosition],
                    data.value
                ) else checkCurrentPosition(it))

            })
        }
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
        REPEAT_OFF(0),
        REPEAT_ONE_SONG(1),
        REPEAT_ALL(2)
    }

    override fun onBluetoothMusicDataChanged(name: String, artist: String) {
        _title.value = name
        _artist.value = artist
        _isPlaying.value = mediaPlayer.isPlaying
    }

    fun startAuxMode() {
        changeSource(0)
        _isAuxModeOn.tryEmit(true)
        Toast.makeText(this, "В разработке.", Toast.LENGTH_LONG).show()
    }


    fun startBtMode() {
        if (!btModeOn().value){
            changeSource(1)
            stopMediaPlayer()
            startBtListener()
        }
    }

    fun startDiskMode(btOn: Boolean) {
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
        if(btModeOn().value) stopBtListener()
        when (sourceEnum) {
            //AUX
            0 -> {
                _isUsbModeOn.tryEmit(false)
                _isDiskModeOn.tryEmit(false)
                _isBtModeOn.tryEmit(false)
            }
            //BT
            1 -> {
                _isPlaying.value = false
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


    fun stopBtListener() {
        try {
            if (btPlaying.value) {
                twManager.playerPlayPause()
                _btPlaying.value = false
            }
            twManager.removeListener(this)
            twManager.stopMonitoring(applicationContext)

        } catch (e: Exception) {

        }

    }

    override fun appClosed() {
        stopMediaPlayer()
        twManagerMusic.close()
    }

    fun startBtListener() {
        onBluetoothMusicDataChanged("", "")
        twManager.startMonitoring(applicationContext) {
//            twManager.addListener(this)
            twManager.requestConnectionInfo()
        }
    }

    fun startMusicListener() {
        twManagerMusic.addListener(this)
    }


    fun stopMusicListener() {
        mediaPlayer.stop()
        twManagerMusic.close()
        twManagerMusic.removeListener(this)
    }

    override fun initTrack(track: Track, data1: String) {
        _isFavorite.value = false
        val currentTrack = track
        updateTracks(mediaManager)
        updateSeekBar()
        val albumID: Long = currentTrack.albumId
        _idSong.value = currentTrack.id.toInt()
        updateMusicName(currentTrack.title, currentTrack.album, currentTrack.duration)
        _data.value = track.data
        getMusicImg(albumID)
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
        }
        queryFavoriteMusic()
    }

    private fun stopMediaPlayer() {
        mediaPlayer.stop()
        tracks.clear()
    }

    override fun testPlay(track: Track) {
        updateTracks(mediaManager)
        val currentTrack = track
        val albumID: Long = currentTrack.albumId
        _idSong.value = currentTrack.id.toInt()
        updateMusicName(currentTrack.title, currentTrack.artist, currentTrack.duration)
        Log.i("onTrackClicked ", "testPley")
        getMusicImg(albumID)
        mediaPlayer.apply {
            stop()
            reset()
            setDataSource(track.data)
            prepare()
        }
        updateSeekBar()

    }

    private fun checkCurrentPosition(data: String) {
        var i = 0
        var q = 0
        CoroutineScope(Dispatchers.IO).launch {
            while (i in tracks.indices) {
                if (tracks[q].data != data) {
                    q++
                    i++
                } else {
                    Log.i(TAG, "checkCurrentPosition $q$data")
                    initTrack(tracks[q], data)
                    break
                }
            }
        }
        currentTrackPosition = q
    }

    override fun firstOpenTrackFound(track: Track) {
        updateTracks(mediaManager)
        val currentTrack = track
        updateMusicName(currentTrack.title, currentTrack.artist, currentTrack.duration)
    }

    override fun playOrPause(): Boolean {
        when (mode) {
            SourceEnum.DISK -> {
                when (isPlaying()) {
                    true -> pause()
                    false -> resume()
                }
            }
            SourceEnum.USB -> {
                when (isPlaying()) {
                    true -> pause()
                    false -> resume()
                }
            }
            SourceEnum.BT -> {
                _btPlaying.value = !btPlaying.value
                twManager.playerPlayPause()
            }
            SourceEnum.AUX -> TODO()
        }
        return isPlaying()
    }


    private fun updateMusicName(title: String, artist: String, duration: String) {
        _title.value = title
        _artist.value = artist
        _duration.value = duration
        _isPlaying.value = mediaPlayer.isPlaying
    }

    override fun getMusicImg(albumID: Long) {
        getMusicCover(GetMusicCover.Params(albumID)) {
            _cover.value = when (it) {
                is Either.Left -> ""
                is Either.Right -> {
                    it.r
                }
                else -> ""
            }
        }
    }

    override fun pause() {
        mediaPlayer.pause()
        _isPlaying.value = mediaPlayer.isPlaying
    }

    override fun resume() {
        if (musicEmpty.value) {

        } else {
            mediaPlayer.start()
            _isPlaying.value = mediaPlayer.isPlaying
        }
    }

    override fun isPlaying(): Boolean = mediaPlayer.isPlaying


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
                    when (currentTrackPosition + 1) {
                        tracks.size -> currentTrackPosition = 0
                        else -> currentTrackPosition++
                    }

                    when (isPlaying()) {
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
                when (currentTrackPosition + 1) {
                    tracks.size -> currentTrackPosition = 0
                    else -> currentTrackPosition++
                }

                when (isPlaying()) {
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

    fun compilationMusic() {
        mediaPlayer.setOnCompletionListener(OnCompletionListener {
            Log.i("isPlayingAutoModeMain", "true${isPlaying.value}")
            if (usbConnectionCheck() && isUsbModeOn.value){
                nextTrack(1)
            } else if (isBtModeOn.value){
                nextTrack(1)
            } else {
                startDiskMode(!isNotConnected.value)
                nextTrack(2)
            }
        })
    }

    fun initMediaPlayer() {
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
                when (repeatMode) {
                    RepeatMusicEnum.REPEAT_OFF -> {
                        funRepeatOff(0)
                    }
                    RepeatMusicEnum.REPEAT_ONE_SONG -> {
                        when (auto) {
                            0 -> funRepeatOff(0)
                            1 -> funPlayOneSong(1)
                            2 -> restartPlaylist(2)
                        }
                    }
                    RepeatMusicEnum.REPEAT_ALL -> {
                        funRepeatAll()
                    }

                }
            }
            SourceEnum.BT -> {
                twManager.playerNext()
            }
            SourceEnum.AUX -> {
                TODO()
            }

            SourceEnum.USB -> {
                when (repeatMode) {
                    RepeatMusicEnum.REPEAT_OFF -> {
                        funRepeatOff(0)
                    }
                    RepeatMusicEnum.REPEAT_ONE_SONG -> {
                        when (auto) {
                            0 -> funRepeatOff(0)
                            1 -> funPlayOneSong(1)
                            2 -> restartPlaylist(2)
                        }
                    }
                    RepeatMusicEnum.REPEAT_ALL -> {
                        funRepeatAll()
                    }
                }

            }
        }
    }

    fun funRepeatAll() {

    }

    private fun restartPlaylist(mode: Int) {
        if (tracks.isEmpty()) {
        } else {
            currentTrackPosition = 0
            funPlayOneSong(mode)
        }
    }

//    fun lastTrackCheck(mode: Int){
//        if (tracks.isEmpty()){
//        } else {
//            when (currentTrackPosition)
//        }
//    }

    fun funRepeatOff(mode: Int) {
        if (tracks.isEmpty()) {
        } else {
            when (currentTrackPosition - 1) {
                -1 -> currentTrackPosition = tracks.size - 1
                else -> currentTrackPosition--
            }
            funPlayOneSong(mode)
        }
    }


    fun funPlayOneSong(mode: Int) {
        when (mode) {
            0 -> {
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
            1 -> {
                when (isPlaying.value) {
                    true -> {
                        initTrack(
                            tracks[currentTrackPosition],
                            tracks[currentTrackPosition].data
                        )
                        resume()
                        Log.i("isPlayingAutoMode", "true${isPlaying.value}")
                    }
                    false -> {
                        initTrack(
                            tracks[currentTrackPosition],
                            tracks[currentTrackPosition].data
                        )
                        Log.i("isPlayingAutoMode", "true${isPlaying.value}")
                    }
                }
            }
            2 -> {
                when (isPlaying.value) {
                    true -> {
                        initTrack(
                            tracks[currentTrackPosition],
                            tracks[currentTrackPosition].data
                        )
                        resume()
                        Log.i("isPlayingAutoMode", "true${isPlaying.value}")
                    }
                    false -> {
                        initTrack(
                            tracks[currentTrackPosition],
                            tracks[currentTrackPosition].data
                        )
                        Log.i("isPlayingAutoMode", "true${isPlaying.value}")
                    }
                }
            }
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
//                ACTION_TOGGLE_PAUSE -> playOrPause()
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
                val result = mediaManager.scanTracks(1)
                if (result is Either.Right) {
                    replaceAllTracks(result.r)
                } else {

                }
            }
            false -> {
                val result = mediaManager.scanTracks(0)
                if (result is Either.Right) {
                    replaceAllTracks(result.r)
                }
            }
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

    override fun changeRepeatMode() {
        when (repeatHowNow.value) {
            0 -> _repeatHowNow.value = 1
            1 -> _repeatHowNow.value = 2
            2 -> _repeatHowNow.value = 0
        }
        howRepeatMode()
    }

    fun howRepeatMode() {
        when (repeatHowNow.value) {
            0 -> repeatOff()
            1 -> oneSongRepeat()
            2 -> allSongsRepeat()
        }
    }

    private fun repeatOff() {
        repeatMode = RepeatMusicEnum.REPEAT_OFF
    }

    private fun oneSongRepeat() {
        repeatMode = RepeatMusicEnum.REPEAT_ONE_SONG
    }

    private fun allSongsRepeat() {
        repeatMode = RepeatMusicEnum.REPEAT_ALL
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
            SourceEnum.DISK ->
                if (isNotConnected.value) {
                    startDiskMode(false)
                } else {
                    startDiskMode(true)
                }
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

    fun usbConnectionCheck(): Boolean {
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


    fun usbTest() {
        //  val presenter = MusicPresenter(context)
        // presenter.openUSBList()
        // twMusic.addRecordUSB("/storage/usbdisk0")
        //twMusic.addRecordUSB("/storage/usbdisk0/Моргенштерн - Я лью кристал.mp3")
        //presenter.musicPlay()

        Log.i("USBlib ", "usbTest: ")
    }

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
    }


}