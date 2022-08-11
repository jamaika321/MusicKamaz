package ru.kamaz.music.cache

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.kamaz.music.cache.db.AppDatabase
import ru.kamaz.music.data.MusicCache
import ru.kamaz.music.domain.FavoriteSongsEntity
import ru.kamaz.music.domain.HistorySongsEntity
import ru.kamaz.music.domain.PlayListEntity
import ru.kamaz.music.domain.TrackEntity
import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.models.ErrorMessage
import ru.kamaz.music_api.models.HistorySongs
import ru.kamaz.music_api.models.PlayListModel
import ru.kamaz.music_api.models.Track
import ru.sir.core.Either
import ru.sir.core.None

class MusicCacheImpl(private val prefsManager: SharedPrefsManager, private val db: AppDatabase) :
    MusicCache {
    override fun getLastMusic(): String = prefsManager.getLastMusic()
    override fun saveLastMusic(lastMusic: String) = prefsManager.saveMusicInfo(lastMusic)
    override fun insertFavoriteSong(song: FavoriteSongsEntity): Either<Failure, None> {
        db.userDao().insertAll(song)
        return Either.Right(None())
    }

    override fun deleteFavoriteSong(song: FavoriteSongsEntity): Either<Failure, None> {
        db.userDao().delete(song)
        return Either.Right(None())
    }

    override fun insertPlayList(song: PlayListEntity): Either<Failure, None> {
        db.playListDao().insertAll(song)
        return Either.Right(None())
    }

    override fun deletePlayList(playList: String): Either<Failure, None> {
        db.playListDao().delete(playList)
        return Either.Right(None())
    }

    override fun queryHistorySongs(id: Int): Either<None, List<HistorySongs>> {
//        return Either.Right(db.historySongsDao().loadAll(id).map { convertEntityToHistorySongs(it) })
        return try {
            Either.Right(db.historySongsDao().loadAll(id).map {convertEntityToHistorySongs(it)})
        } catch (e: Exception) {
            Log.i("ReviewTest_LastMusic", "queryHistorySongs: catch ")
            Either.Left(None())
        }
    }

    override fun updatePlayList(name: String, data: List<String>) {
        db.playListDao().updatePlayList(name, data)
    }

    override fun updatePlayListName(name: String, newName: String) {
        db.playListDao().updatePlayListName(name, newName)
    }

    override fun getTrackList(): List<TrackEntity> {
        return db.historySongsDao().getTrackList()
    }

    override fun insertHistorySong(song: HistorySongsEntity): Either<Failure, None> {
        db.historySongsDao().insertAll(song)
        Log.i("ReviewTest_LastMusic", "insertHistorySong:  ")
        return Either.Right(None())
    }

    override fun queryFavoriteSongs(data: String): Either<Failure, String> {
        return try {
            Either.Right(db.userDao().loadAll(data).data)
        } catch (e: Exception) {
            Either.Left(
                Failure.AuthorizationError(
                    ErrorMessage(
                        404,
                        e.message.toString(),
                        e.localizedMessage ?: ""
                    )
                )
            )
        }
    }

    override fun getAllFavoriteSongs(): Flow<List<Track>> {
        return db.userDao().getData().map { convertEntityListFavoriteModelList(it) }
    }

    override fun getAllPlayList(): Flow<List<PlayListModel>> {
        return db.playListDao().getData().map { convertEntityPlayListModelList(it) }
    }

    private fun convertEntityListFavoriteModelList(entity: List<FavoriteSongsEntity>): List<Track> {
        val data = mutableListOf<Track>()
        entity.forEach {
            data.add(
                Track(
                    it.id,
                    it.title,
                    it.artist,
                    it.data,
                    it.duration,
                    it.album,
                    it.albumArt,
                    it.playing,
                    it.favorite
                )
            )
        }
        return data
    }

    private fun convertEntityToHistorySongs(entity: HistorySongsEntity): HistorySongs {
        val data = HistorySongs(
            entity.id,
            entity.data,
            entity.timePlayed,
            entity.source,
            entity.sourceName,
        )
        return data
    }

    private fun convertEntityPlayListModelList(entity: List<PlayListEntity>): List<PlayListModel> {
        val data = mutableListOf<PlayListModel>()
        entity.forEach {
            data.add(
                PlayListModel(
                    it.idPlayList,
                    it.name,
                    it.albumArt,
                    it.trackDataList as ArrayList<String>
                )
            )
        }
        return data
    }

}