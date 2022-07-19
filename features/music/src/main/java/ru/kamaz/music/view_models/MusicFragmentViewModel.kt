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
import ru.kamaz.music.domain.TestSettings
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music_api.domain.GetFilesUseCase
import ru.kamaz.music_api.interactor.GetMusicPosition
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.BaseViewModel
import javax.inject.Inject

class MusicFragmentViewModel @Inject constructor(
    application: Application,
    private val testSettings: TestSettings,
    private val getMusicPosition: GetMusicPosition,
) : BaseViewModel(application), MediaPlayer.OnCompletionListener, ServiceConnection,
    MusicServiceInterface.ViewModel, MusicManagerListener {

    val artist: StateFlow<String> by lazy {
        service.value?.getArtistName() ?: MutableStateFlow("Unknown")
    }

    val isPlay: StateFlow<Boolean> by lazy {
        service.value?.isPlay() ?: MutableStateFlow(false)
    }

    val repeatHowModeNow: StateFlow<Int> by lazy {
        service.value?.getRepeat() ?: MutableStateFlow(2)
    }

    val title: StateFlow<String> by lazy {
        service.value?.getMusicName() ?: MutableStateFlow("Unknown")
    }

    val duration: StateFlow<Int> by lazy {
        service.value?.getMusicDuration() ?: MutableStateFlow(0)
    }

    val isShuffleOn: StateFlow<Boolean> by lazy {
        service.value?.isShuffleOn() ?: MutableStateFlow(true)
    }

    val isNotConnected: StateFlow<Boolean> by lazy {
        service.value?.checkDeviceConnection() ?: MutableStateFlow(true)
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

    val lastMusic: StateFlow<String> by lazy {
        service.value?.lastMusic() ?: MutableStateFlow("")
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
        service.value?.dialogFragment() ?: MutableStateFlow(false)
    }

    val isMusicEmpty: StateFlow<Boolean> by lazy {
        service.value?.musicEmpty() ?: MutableStateFlow(false)
    }

    val cover: StateFlow<String> by lazy {
        service.value?.coverId() ?: MutableStateFlow("")
    }

    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()

    var musicPosition: StateFlow<Int> =
        getMusicPosition().stateIn(viewModelScope, SharingStarted.Lazily, 0)


    override fun init() {
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        remoteNextPrevControlButton()

    }

    fun remoteNextPrevControlButton() {
        testSettings.start {
            when (it) {
                19 -> nextTrack()
                21 -> previousTrack()
            }
        }
    }


    fun shuffleStatusChange() {
        service.value?.shuffleStatusChange()
    }

    fun playOrPause() {
        service.value?.playOrPause()
    }

    fun checkPosition(position: Int) {
        service.value?.checkPosition(position)
    }

    fun previousTrack() {
        service.value?.previousTrack()
    }

    fun isSaveFavoriteMusic() {
        service.value?.insertFavoriteMusic()
    }

    fun isSaveLastMusic() {
        service.value?.insertLastMusic()
    }

    fun nextTrack() {
        service.value?.nextTrack(0)
    }

    fun appClosed() {
        service.value?.appClosed()
    }

    fun lastSavedState() {
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

    fun repeatChange() {
        service.value?.changeRepeatMode()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        nextTrack()
    }

    override fun onDestroy() {
        testSettings.stop()
        super.onDestroy()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d("testPlayTrack", "onServiceConnected")
        _service.value = (service as MusicService.MyBinder).getService()
        this.service.value?.setViewModel(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _service.value = null
    }


    override fun onSdStatusChanged(path: String, isAdded: Boolean) {
        "MicroSD status changed: value = $path status = $isAdded".easyLog(this)
    }

    override fun onUsbStatusChanged(path: String, isAdded: Boolean) {
        "USB status changed: value = $path status = $isAdded".easyLog(this)
    }


}