package ru.kamaz.music.data

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.kamaz.music.domain.FavoriteSongsEntity
import ru.kamaz.music.domain.HistorySongsEntity
import ru.kamaz.music.domain.PlayListEntity
import ru.kamaz.music.domain.TrackEntity
import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.SourceType
import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.*
import ru.sir.core.Either
import ru.sir.core.None
import java.io.File


class RepositoryImpl(
    private val media: MediaManager,
    private val mediaPlayer: MediaPlayer,
    private val testDBDao: MusicCache
) : Repository {
    override fun loadDiskData(mode: String): Either<None, List<Track>> = media.getMediaFilesFromPath("all", mode)
    override fun loadUsbData(mode: String): Either<None, List<Track>> = media.getMediaFilesFromPath("sdCard", mode)
    override fun rvArtist(): Either<None, List<Track>> = media.getMediaFilesFromPath("storage", "all")
    override fun rvPlayList(): Flow<List<PlayListModel>> = testDBDao.getAllPlayList()
    override fun rvCategory(): Either<None, List<CategoryMusicModel>> = media.getCategory()
    override fun rvFavorite(): Either<None, List<Track>> = testDBDao.getAllFavoriteSongs()
    override fun rvAllFolderWithMusic(): Either<None, List<AllFolderWithMusic>> =
        media.getAllFolder()

    override fun getMusicCover(albumId: Long): Either<None, String> =
        media.getAlbumImagePath(albumId)

    override fun getMusicPositionFlow(): Flow<Int> = flow {
        while (true) {
            val currentPosition = mediaPlayer.currentPosition
            emit(currentPosition)
            delay(1000)
        }
    }
    override fun getMusicDurationFlow(): Flow<Int> = flow {
        while (true) {
            val currentPosition = mediaPlayer.currentPosition
            emit(currentPosition)
            delay(1000)
        }
    }


    override fun lastTrack(): Either<None, String> {
        TODO("Not yet implemented")
    }

    override fun insertFavoriteSong(song: Track): Either<Failure, None> =
        testDBDao.insertFavoriteSong(song.toFavoriteDao())

    override fun deleteFavoriteSong(song: Track): Either<Failure, None> =
        testDBDao.deleteFavoriteSong(song.toFavoriteDao())

    override fun insertPlayList(song: PlayListModel): Either<Failure, None> =
        testDBDao.insertPlayList(song.toDao())

    override fun deletePlayList(song: PlayListModel): Either<Failure, None> =
        testDBDao.deletePlayList(song.toDao())

    override fun insertHistorySong(song: HistorySongs): Either<Failure, None> {
        val r = testDBDao.insertHistorySong(song.toDao())
        return r
    }

    override fun queryFavoriteSongs(data: String): Either<Failure, String> =
        testDBDao.queryFavoriteSongs(data)

    // override fun queryFavoriteSongs():  Either<Failure, String> = testDBDao.queryFavoriteSongs()

    override fun queryHistorySongs(): Either<Failure, String> = testDBDao.queryHistorySongs()
    override fun getCurrentPath(): String {
        TODO("Not yet implemented")
    }

    override fun getRootPathOfSource(): String {
        TODO("Not yet implemented")
    }

    override fun getSelectedSource(): SourceType {
        TODO("Not yet implemented")
    }

    private fun Track.toFavoriteDao() = FavoriteSongsEntity(
        this.id,
        this.title,
        this.artist,
        this.data,
        this.genre,
        this.duration,
        this.album,
        this.albumArt,
        this.playing,
        this.favorite)
    private fun PlayListModel.toDao() = PlayListEntity(this.id, this.title, this.albumArt, this.trackTitleList, this.trackDataList)
    private fun HistorySongs.toDao() = HistorySongsEntity(
        this.dbID,
        this.idCursor,
        this.title,
        this.trackNumber,
        this.year,
        this.duration,
        this.data,
        this.dateModified,
        this.albumArt,
        this.albumName,
        this.artistId,
        this.artistName,
        this.albumArtist,
        this.albumArtist,
        this.timePlayed
    )
    private fun Track.toDao() = TrackEntity(
        this.id,
        this.title,
        this.artist,
        this.data,
        this.genre,
        this.duration,
        this.album,
        this.albumArt,
        this.playing
    )
    private fun TrackEntity.fromDao() = Track(
        this.id,
        this.title,
        this.artist,
        this.data,
        this.genre,
        this.duration,
        this.album,
        this.albumArt,
        this.playing
    )

    private val devicePath = "/storage/usbdisk0"

    private var pathSourceRootDirectory: String = devicePath
    private var pathSelectedDirectory: String = devicePath
    private var sourceType: SourceType = SourceType.DEVICE

    override suspend fun rootFilesFromSource(source: SourceType): List<File> {
        sourceType = source
        val listFiles = when (source) {
            SourceType.NO_MEDIA -> getNoMediaFiles()
            else -> getFiles(devicePath) //пока так
        }
        pathSelectedDirectory = pathSourceRootDirectory
        return listFiles
    }


    override fun getFiles(path: String): List<File> {
        val file = File(path)
        val list = file.listFiles().toList().sorted()
        Log.i("usbMusic", "getFiles: $list")
        return (list)
    }

    private fun readMediaStore(media1: SourceType) {

    }
    private fun getNoMediaFiles(): List<File> {
        val resultList = mutableListOf<File>()
        val allFiles = File(devicePath).listFiles()
        val mimeList = arrayOf(
            "mp3",
            "mp4"
        )
        for (item in allFiles) {
            //если это не Папка
            if (!item.isDirectory) {
                val uriFile: Uri = Uri.fromFile(item)
                val mime = MimeTypeMap.getFileExtensionFromUrl(uriFile.toString())
                //если расширение не входит в перечень - добавляем
                if (mime !in mimeList) resultList.add(item)
            }

        }
        return resultList
    }


}