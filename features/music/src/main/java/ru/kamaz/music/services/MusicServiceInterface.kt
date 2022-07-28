package ru.kamaz.music.services

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import ru.kamaz.music.data.MediaManager
import ru.kamaz.music_api.models.AllFolderWithMusic
import ru.kamaz.music_api.models.PlayListModel
import ru.kamaz.music_api.models.PlayListSource
import ru.kamaz.music_api.models.Track


interface MusicServiceInterface{
    interface Service{
        fun setViewModel(viewModel:ViewModel)
        fun init()
        fun playOrPause(): Boolean
        fun pause()
        fun resume()
        fun isPlay(): StateFlow<Boolean>
        fun checkPosition(position: Int)
        fun previousTrack()
        fun nextTrack(auto:Int)
        fun updateTracks(loadMode: String)
        fun intMediaPlayer()
        fun sourceSelection(action: MusicService.SourceEnum)
        fun getAllTracks(): StateFlow<List<Track>>
        fun getPlayLists(): StateFlow<List<PlayListModel>>
        fun getFoldersList(): StateFlow<List<AllFolderWithMusic>>
        fun getMusicName(): StateFlow<String>
        fun getArtistName(): StateFlow<String>
        fun getMusicDuration(): StateFlow<Int>
        fun checkDeviceConnection(): StateFlow<Boolean>
        fun lastMusic(): StateFlow<String>
        fun checkUSBConnection(): StateFlow<Boolean>
        fun updateWidget():StateFlow<Boolean>
        fun btModeOn():StateFlow<Boolean>
        fun playListModeOn(): StateFlow<String>
        fun auxModeOn():StateFlow<Boolean>
        fun diskModeOn():StateFlow<Boolean>
        fun usbModeOn():StateFlow<Boolean>
        fun howModeNow():Int
        fun dialogFragment():StateFlow<Boolean>
        fun musicEmpty():StateFlow<Boolean>
        fun coverId(): StateFlow<String>
        fun isFavoriteMusic():StateFlow<Boolean>
        fun insertFavoriteMusic(data: String)
        fun shuffleStatusChange()
        fun insertLastMusic()
        fun getRepeat(): StateFlow<Int>
        fun changeRepeatMode()
        fun isShuffleOn(): StateFlow<Boolean>
        fun getMusicData(): StateFlow<String>
        fun initTrack(track: Track, data1: String)
        fun clearTrackData()
        fun appClosed()
        fun lastSavedState()
        fun checkUsb()
        fun initPlayListSource(track: Track, playList: PlayListSource)
        fun getSource(): StateFlow<String>
    }

    interface ViewModel{
    }

}