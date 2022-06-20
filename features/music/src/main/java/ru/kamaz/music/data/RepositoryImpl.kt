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
    override fun loadDiskData(): Either<None, List<Track>> = media.getMediaFilesFromPath("storage")
    override fun rvArtist(): Either<None, List<Track>> = media.getMediaFilesFromPath("storage")
    override fun rvPlayList(): Flow<List<PlayListModel>> = testDBDao.getAllPlayList()
    override fun rvCategory(): Either<None, List<CategoryMusicModel>> = media.getCategory()
    override fun rvFavorite(): Flow<List<FavoriteSongs>> = testDBDao.getAllFavoriteSongs()
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

    override fun insertFavoriteSong(song: FavoriteSongs): Either<Failure, None> =
        testDBDao.insertFavoriteSong(song.toDao())

    override fun deleteFavoriteSong(song: FavoriteSongs): Either<Failure, None> =
        testDBDao.deleteFavoriteSong(song.toDao())

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

    private fun FavoriteSongs.toDao() = FavoriteSongsEntity(this.idSong, this.data,this.title,this.artist)
    private fun PlayListModel.toDao() = PlayListEntity(this.id, this.title)
    private fun HistorySongs.toDao() = HistorySongsEntity(
        this.dbID,
        this.idCursor,
        this.title,
        this.trackNumber,
        this.year,
        this.duration,
        this.data,
        this.dateModified,
        this.albumId,
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

    override fun insertTrackList(track: List<Track>): Either<Failure, None> {
        var trackEntity = ArrayList<TrackEntity>()
        for (i in track.indices){
            trackEntity.add(i, track[i].toDao())
        }
        return testDBDao.insertTrackList(trackEntity)
    }

    override fun getTrackList(): Either<Failure, List<Track>> {
        var track = ArrayList<Track>()
        for (i in testDBDao.getTrackList().indices) {
            track.add(testDBDao.getTrackList()[i].fromDao())
        }
        return Either.Right(track)
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