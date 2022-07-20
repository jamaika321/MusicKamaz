package ru.kamaz.music.data

import kotlinx.coroutines.flow.Flow
import ru.kamaz.music.domain.FavoriteSongsEntity
import ru.kamaz.music.domain.HistorySongsEntity
import ru.kamaz.music.domain.PlayListEntity
import ru.kamaz.music.domain.TrackEntity
import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interactor.InsertPlayList
import ru.kamaz.music_api.models.FavoriteSongs
import ru.kamaz.music_api.models.PlayListModel
import ru.kamaz.music_api.models.Track
import ru.sir.core.Either
import ru.sir.core.None

interface MusicCache {
    fun getLastMusic():String
    fun saveLastMusic(lastMusic:String)
    fun insertFavoriteSong(song: FavoriteSongsEntity): Either<Failure, None>
    fun deleteFavoriteSong(song: FavoriteSongsEntity): Either<Failure, None>
    fun insertPlayList(song: PlayListEntity): Either<Failure, None>
    fun deletePlayList(playList: String): Either<Failure, None>
    fun insertHistorySong(song: HistorySongsEntity): Either<Failure, None>
    fun queryFavoriteSongs(data:String) :  Either<Failure, String>
    fun getAllFavoriteSongs(): Flow<List<Track>>
    fun getAllPlayList(): Flow<List<PlayListModel>>
    fun queryHistorySongs(): Either<Failure, String>

    fun insertTrackList(tracks: List<TrackEntity>) : Either<Failure, None>
    fun getTrackList(): List<TrackEntity>
}