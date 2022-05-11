package ru.kamaz.music.data

import kotlinx.coroutines.flow.Flow
import ru.kamaz.music.domain.FavoriteSongsEntity
import ru.kamaz.music.domain.HistorySongsEntity
import ru.kamaz.music.domain.PlayListEntity
import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.models.FavoriteSongs
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.core.Either
import ru.sir.core.None

interface MusicCache {
    fun getLastMusic():String
    fun saveLastMusic(lastMusic:String)
    fun insertFavoriteSong(song: FavoriteSongsEntity): Either<Failure, None>
    fun deleteFavoriteSong(song: FavoriteSongsEntity): Either<Failure, None>
    fun insertPlayList(song: PlayListEntity): Either<Failure, None>
    fun deletePlayList(song: PlayListEntity): Either<Failure, None>
    fun insertHistorySong(song: HistorySongsEntity): Either<Failure, None>
    fun queryFavoriteSongs(data:String) :  Either<Failure, String>
    fun getAllFavoriteSongs():  Flow<List<FavoriteSongs>>
    fun getAllPlayList(): Flow<List<PlayListModel>>
    fun queryHistorySongs(): Either<Failure, String>
}