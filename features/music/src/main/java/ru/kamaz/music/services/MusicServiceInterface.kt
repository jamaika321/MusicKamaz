package ru.kamaz.music.services

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import ru.kamaz.music.data.MediaManager
import ru.kamaz.music_api.models.Track


interface MusicServiceInterface{
    interface Service{
        fun setViewModel(viewModel:ViewModel)
        fun init()
        fun playOrPause(): Boolean
        fun firstOpenTrackFound(track: Track)
        fun getMusicImg(albumID: String)
        fun pause()
        fun resume()
        fun isPlay(): StateFlow<Boolean>
        fun checkPosition(position: Int)
        fun previousTrack()
        fun nextTrack(auto:Int)/* fun updateMusic(track: Track)*/
        fun updateTracks(mediaManager: MediaManager)
        fun intMediaPlayer()
        fun sourceSelection(action: MusicService.SourceEnum)
        fun getMusicName(): StateFlow<String>
        fun getArtistName(): StateFlow<String>
        fun getMusicDuration(): StateFlow<String>
        fun checkDeviceConnection(): StateFlow<Boolean>
        fun lastMusic(): StateFlow<String>
        fun checkUSBConnection(): StateFlow<Boolean>
        fun checkBTConnection(): StateFlow<Boolean>
        fun updateWidget():StateFlow<Boolean>
        fun btModeOn():StateFlow<Boolean>
        fun auxModeOn():StateFlow<Boolean>
        fun diskModeOn():StateFlow<Boolean>
        fun usbModeOn():StateFlow<Boolean>
        fun usbConnect():StateFlow<Boolean>
        fun howModeNow():Int
        fun dialogFragment():StateFlow<Boolean>
        fun musicEmpty():StateFlow<Boolean>
        fun coverId(): StateFlow<String>
        fun isFavoriteMusic():StateFlow<Boolean>
        fun insertFavoriteMusic()
        fun shuffleStatusChange()
        fun deleteFavoriteMusic()
        fun insertLastMusic()
        fun getRepeat(): StateFlow<Int>
        fun changeRepeatMode()
        fun isShuffleOn(): StateFlow<Boolean>
        fun changeRv(): StateFlow<Int>
        fun isChangeRv()
        fun initTrack(track: Track, data1: String)
        fun clearTrackData()
        fun appClosed()
        fun lastSavedState()
        fun usbConnectionCheck(): Boolean
    }

    interface ViewModel{
        fun addListener()
        fun removeListener()
        fun onCheckPosition(position: Int)
        fun onUpdateSeekBar(duration:Int)
        fun selectBtMode()
    }

}