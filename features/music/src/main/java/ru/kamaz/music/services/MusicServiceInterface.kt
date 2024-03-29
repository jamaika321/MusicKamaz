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
        fun updateTracks(path: String, loadMode: String)
        fun sourceSelection(action: MusicService.SourceEnum)
        fun getAllTracks(): StateFlow<List<Track>>
        fun getPlayLists(): StateFlow<List<PlayListModel>>
        fun getFoldersList(): StateFlow<List<AllFolderWithMusic>>
        fun lastMusic(): StateFlow<String>
        fun checkUSBConnection(): StateFlow<Boolean>
        fun replaceAllTracks(trackList: List<Track>, replace: Boolean)
        fun btModeOn():StateFlow<Boolean>
        fun defaultModeOn(): StateFlow<Boolean>
        fun playListModeOn(): StateFlow<String>
        fun auxModeOn():StateFlow<Boolean>
        fun diskModeOn():StateFlow<Boolean>
        fun usbModeOn():StateFlow<Boolean>
        fun howModeNow():Int
        fun dialogFragment():StateFlow<Boolean>
        fun musicEmpty():StateFlow<Boolean>
        fun insertFavoriteMusic(data: String)
        fun shuffleStatusChange()
        fun insertLastMusic(id: Int)
        fun getRepeat(): StateFlow<Int>
        fun changeRepeatMode()
        fun fillSelectedTrack()
        fun isShuffleOn(): StateFlow<Boolean>
        fun initTrack(track: Track, data1: String)
        fun appClosed()
        fun lastSavedState()
        fun checkUsb()
        fun initPlayListSource(playList: PlayListSource)
        fun getSource(): StateFlow<String>
        fun getTrackInfo(): StateFlow<Track>
//        fun coverId(): StateFlow<String>
        fun isFavoriteMusic():StateFlow<Boolean>
//        fun getMusicData(): StateFlow<String>
//        fun getMusicName(): StateFlow<String>
//        fun getArtistName(): StateFlow<String>
//        fun getMusicDuration(): StateFlow<Int>
    }

    interface ViewModel{
    }

}