package ru.kamaz.music.cache

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
import ru.kamaz.music_api.models.FavoriteSongs
import ru.kamaz.music_api.models.PlayListModel
import ru.kamaz.music_api.models.Track
import ru.sir.core.Either
import ru.sir.core.None
import java.lang.Exception

class MusicCacheImpl (private val prefsManager: SharedPrefsManager, private val db: AppDatabase):MusicCache {
    override fun getLastMusic(): String = prefsManager.getLastMusic()
    override fun saveLastMusic(lastMusic: String)  = prefsManager.saveMusicInfo(lastMusic)
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

    override fun deletePlayList(song: PlayListEntity): Either<Failure, None> {
        db.playListDao().delete(song)
        return Either.Right(None())
    }

    override fun queryHistorySongs(): Either<Failure, String> {
        return try {
            Either.Right(db.historySongsDao().getLastMusic().title)
        } catch (e: Exception) {
            Either.Left(Failure.AuthorizationError(ErrorMessage(404, e.message.toString(), e.localizedMessage ?: "")))
        }
    }

    override fun insertTrackList(tracks: List<TrackEntity>) : Either<Failure, None> {
        db.historySongsDao().insertTrackList(tracks)
        return Either.Right(None())
    }

    override fun getTrackList(): List<TrackEntity> {
        return db.historySongsDao().getTrackList()
    }

    override fun insertHistorySong(song: HistorySongsEntity): Either<Failure, None> {
         db.historySongsDao().insertAll(song)
        return Either.Right(None())
    }

    override fun queryFavoriteSongs(data: String): Either<Failure, String> {
        return try {
            Either.Right(db.userDao().loadAll(data).data)
        } catch (e: Exception) {
            Either.Left(Failure.AuthorizationError(ErrorMessage(404, e.message.toString(), e.localizedMessage ?: "")))
        }
    }

    override fun getAllFavoriteSongs(): Either<None, List<Track>> {
        return try {
            Either.Right( convertEntityListFavoriteModelList(db.userDao().getData()) )
        } catch (e: Exception) {
            Either.Left(None())
        }
    }

    override fun getAllPlayList(): Flow<List<PlayListModel>> {
        return db.playListDao().getData().map { convertEntityPlayListModelList(it) }
    }

    private fun convertEntityListFavoriteModelList(entity: List<FavoriteSongsEntity>): List<Track>{
        val data = mutableListOf<Track>()
        entity.forEach { data.add(Track(
            it.id,
            it.title,
            it.artist,
            it.data,
            it.genre,
            it.duration,
            it.album,
            it.albumArt,
            it.playing,
            it.favorite)) }
        return data
    }

    private fun convertEntityPlayListModelList(entity: List<PlayListEntity>):List<PlayListModel>{
        val data = mutableListOf<PlayListModel>()
        entity.forEach { data.add(PlayListModel(it.idPlayList,it.name, it.albumArt)) }
        return data
    }

}