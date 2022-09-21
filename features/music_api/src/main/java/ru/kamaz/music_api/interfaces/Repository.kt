package ru.kamaz.music_api.interfaces

import kotlinx.coroutines.flow.Flow
import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.SourceType
import ru.kamaz.music_api.models.*
import ru.sir.core.Either
import ru.sir.core.None
import java.io.File

interface Repository: PathManager, SourceManager {
    fun getMusicDurationFlow(): Flow<Int>
    fun rvPlayList(): Flow<List<PlayListModel>>
    fun rvFavorite(): Flow<List<Track>>
    fun rvAllFolderWithMusic():Either<None, List<AllFolderWithMusic>>
    fun getMusicPositionFlow(): Flow<Int>
    fun lastTrack(): Either<None, String>
    fun insertFavoriteSong(song: Track): Either<Failure, None>
    fun deleteFavoriteSong(song: Track): Either<Failure, None>
    fun insertPlayList(song: PlayListModel): Either<Failure, None>
    fun deletePlayList(playList: String): Either<Failure, None>
    fun insertHistorySong(song: HistorySongs): Either<Failure, None>
    fun queryFavoriteSongs(data:String) :  Either<Failure, String>
    fun queryHistorySongs(id: Int) : Either<None, List<HistorySongs>>
    suspend fun rootFilesFromSource(source: SourceType):List<File>
    fun getFiles(path: String): List<File>
    fun updatePlayList(name: String,  data: List<String>): Either<Failure, None>
    fun updatePlayListName(name: String, newName: String): Either<Failure, None>
}