package ru.kamaz.music.data

import android.content.Context
import ru.kamaz.music.services.MusicService
import ru.kamaz.music_api.SourceType
import ru.kamaz.music_api.models.*
import ru.sir.core.Either
import ru.sir.core.None
import java.io.File

interface MediaManager {

//    fun scanTracksData(type: Int): Either<None, List<Track>
    fun scanTracks(type:Int): Either<None, List<Track>>
//    fun scanUSBTracks(path:String): Either<None, List<Track>>
    fun getAlbumImagePath(albumID: Long): Either<None, String>
    fun getCategory():Either<None, List<CategoryMusicModel>>
    fun getAllFolder(): Either<None, List<AllFolderWithMusic>>
    fun scanFoldersWithMusic(sourceType: SourceType): List<File>
    fun getFilesFromPath(path: String, bool1:Boolean,bool2:Boolean): List<File>

   // fun getFolderWithMusic():Either<None, List<FolderMusicModel>>

}