package ru.kamaz.music.view_models

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import ru.biozzlab.twmanager.domain.interfaces.MusicManagerListener
import ru.biozzlab.twmanager.utils.easyLog
import ru.kamaz.music.data.MediaManager
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music_api.domain.GetFilesUseCase
import ru.kamaz.music_api.interactor.GetMusicPosition
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.BaseViewModel
import javax.inject.Inject

class MusicFragmentViewModel @Inject constructor(
    application: Application,
    private val mediaManager: MediaManager,
    private val getMusicPosition: GetMusicPosition,
    private val getFilesUseCase: GetFilesUseCase
) : BaseViewModel(application), MediaPlayer.OnCompletionListener, ServiceConnection,
    MusicServiceInterface.ViewModel,MusicManagerListener {
    private var tracks = ArrayList<Track>()
    private var currentTrackPosition = 0

    private val _btModeActivation = MutableStateFlow(false)
    val btModeActivation = _btModeActivation.asStateFlow()

    val artist: StateFlow<String> by lazy {
      service.value?.getArtistName() ?: MutableStateFlow("Unknown")
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    val repeatHowModeNow: StateFlow<Int> by lazy {
      service.value?.getRepeat() ?: MutableStateFlow(0)
    }

    val title: StateFlow<String> by lazy {
        service.value?.getMusicName() ?: MutableStateFlow("Unknown")
    }

    val duration: StateFlow<String> by lazy {
        service.value?.getMusicDuration() ?: MutableStateFlow("--:--")
    }

    val isShuffleOn: StateFlow<Boolean> by lazy {
        service.value?.isShuffleOn() ?: MutableStateFlow(true)
    }

    val isNotConnected: StateFlow<Boolean> by lazy {
        service.value?.checkDeviceConnection() ?: MutableStateFlow(true)
    }

    val isNotConnectedBt: StateFlow<Boolean> by lazy {
        service.value?.checkBTConnection() ?: MutableStateFlow(true)
    }


    val isFavoriteMusic: StateFlow<Boolean> by lazy {
        service.value?.isFavoriteMusic() ?: MutableStateFlow(true)
    }

    val isNotConnectedUsb: StateFlow<Boolean> by lazy {
        service.value?.checkUSBConnection() ?: MutableStateFlow(false)
    }

    val isAuxModeOn: StateFlow<Boolean> by lazy {
        service.value?.auxModeOn() ?: MutableStateFlow(true)
    }

    val isBtModeOn: StateFlow<Boolean> by lazy {
        service.value?.btModeOn() ?: MutableStateFlow(true)
    }

    val isDiskModeOn: StateFlow<Boolean> by lazy {
        service.value?.diskModeOn() ?: MutableStateFlow(true)
    }

    val isUsbModeOn: StateFlow<Boolean> by lazy {
        service.value?.usbModeOn() ?: MutableStateFlow(false)
    }

    val isDeviceNotConnectFromBt: StateFlow<Boolean> by lazy {
        service.value?.dialogFragment()?: MutableStateFlow(false)
    }

    val isMusicEmpty: StateFlow<Boolean> by lazy {
        service.value?.musicEmpty()?: MutableStateFlow(false)
    }

    private val _cover = MutableStateFlow("")
    val cover = _cover.asStateFlow()

    private val _mode = MutableStateFlow("")
    val mode = _mode.asStateFlow()


    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()

    var musicPosition: StateFlow<Int> =
        getMusicPosition().stateIn(viewModelScope, SharingStarted.Lazily, 0)

    private val _maxSeek = MutableStateFlow(0)
    val maxSeek = _maxSeek.asStateFlow()

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun init() {
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onCreate() {
      //updateTracks(mediaManager)
        super.onCreate()
    }


    fun shuffleStatusChange(){
        service.value?.shuffleStatusChange()
    }

    fun playOrPause() {
        _isPlaying.value = service.value?.playOrPause()?: false
    }

    fun checkPosition(position: Int) {
        service.value?.checkPosition(position)
    }

    fun previousTrack() {
        service.value?.previousTrack()
    }

    fun isSaveFavoriteMusic(){
        service.value?.insertFavoriteMusic()
    }

    fun isSaveLastMusic(){
        service.value?.insertLastMusic()
    }

    fun nextTrack() {
        service.value?.nextTrack(0)
    }

    fun appClosed() {
        service.value?.appClosed()
    }
    fun updateTracks() {
        service.value?.nextTrack(2)
    }

    fun lastSavedState(){
        service.value?.lastSavedState()
    }


    fun vmSourceSelection(action: MusicService.SourceEnum) {
        when (action) {
            MusicService.SourceEnum.AUX -> {
                service.value?.sourceSelection(action)
            }

            MusicService.SourceEnum.BT -> {
                service.value?.sourceSelection(action)
            }

            MusicService.SourceEnum.DISK -> {
                service.value?.sourceSelection(action)
            }
            MusicService.SourceEnum.USB -> {
                service.value?.sourceSelection(action)
            }
        }

    }

    fun repeatChange(){
        service.value?.changeRepeatMode()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        nextTrack()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d("testPlayTrack", "onServiceConnected")
            _service.value = (service as MusicService.MyBinder).getService()
            this.service.value?.setViewModel(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _service.value = null
    }

    override fun addListener() {
        TODO("Not yet implemented")
    }

    override fun removeListener() {
        TODO("Not yet implemented")
    }

    override fun onCheckPosition(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onUpdateSeekBar(duration: Int) {
        _maxSeek.value = duration
    }


    override fun onSdStatusChanged(path: String, isAdded: Boolean) {
        "MicroSD status changed: value = $path status = $isAdded".easyLog(this)
    }

    override fun onUsbStatusChanged(path: String, isAdded: Boolean) {
        "USB status changed: value = $path status = $isAdded".easyLog(this)
    }

    override fun selectBtMode() {

    }


}