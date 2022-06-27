package ru.kamaz.music.media

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import ru.kamaz.music.R
import ru.kamaz.music.data.MediaManager
import ru.kamaz.music_api.models.AllFolderWithMusic
import ru.kamaz.music_api.models.CategoryMusicModel
import ru.kamaz.music_api.models.ModelTest
import ru.kamaz.music_api.models.Track
import ru.sir.core.Either
import ru.sir.core.Either.Left
import ru.sir.core.None
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.random.Random


class AppMediaManager @Inject constructor(val context: Context) : MediaManager {

    private lateinit var metaRetriver: MediaMetadataRetriever


    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAllTracks(): Either<None, List<Track>> {
        val allTracks = ArrayList<Track>()

        var sdCard = scanMediaFilesInSdCard("all")
        if (sdCard is Either.Right){
            allTracks.addAll(sdCard.r)
        }
        sdCard = scanMediaFilesInStorage()
        if (sdCard is Either.Right){
            allTracks.addAll(sdCard.r)
        }
        return Either.Right(allTracks)
    }

    private fun scanMediaFilesInSdCard(mode: String): Either<None, List<Track>> {
        metaRetriver = MediaMetadataRetriever()

        val listWithTrackData = ArrayList<Track>()
        val trackPaths = scanTracksPath()

        if (trackPaths is Either.Right)
        {
            if (mode == "all") {
                for (i in 0 until trackPaths.r.size) {
                    metaRetriver.setDataSource(trackPaths.r[i])

                    val artist =
                        metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ARTIST))
                            ?: ("unknown")
                    val album =
                        metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ALBUM))
                            ?: ("unknown")
                    val title =
                        metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_TITLE))
                            ?: ("unknown")
                    val duration =
                        metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_DURATION))
                            ?.toLong() ?: (180)
                    val albumArtist =
                        metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST))
                            ?: ("unknown")
                    val data = trackPaths.r[i]

                    listWithTrackData.add(
                        Track(
                            Random.nextLong(),
                            title,
                            artist,
                            data,
                            duration,
                            album,
                            ""
                        )
                    )
                }
            } else {
                metaRetriver.setDataSource(trackPaths.r[0])
                listWithTrackData.add(
                    Track(
                        Random.nextLong(),
                        "unknown",
                        "unknown",
                        "unknown",
                        Random.nextLong(),
                        "unknown",
                        "unknown",
                    )
                )
            }
        }
        return if (listWithTrackData.isEmpty()) {
            Either.Left(None())
        }else{
            Either.Right(listWithTrackData)
        }

    }


    private fun scanTracksPath(): Either<None, List<String>> {
        val store = "/storage/usbdisk0"
        lateinit var list: List<String>

        list = readRecursive(File(store), listOf("mp3", "wav")).sorted().map {
            it.toString()
        }
        return if (list.isEmpty()) {
            Either.Left(None())
        } else {
            Either.Right(list)
        }
    }

    private fun readRecursive(root: File, extentions: List<String>): List<File> {
        val list = ArrayList<File>()
        root.listFiles()?.filter { it.isDirectory }?.forEach {
            list.addAll(readRecursive(it, extentions))
        }

        root.listFiles()?.filter { extentions.contains(it.extension) }?.forEach {
            list.add(it)
        }
        return list
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getMediaFilesFromPath(path: String, mode: String): Either<None, List<Track>> {
        Log.i("ReviewTest", "getMediaFilesFromPath: $path ")
        return when (path) {
            "sdCard" -> scanMediaFilesInSdCard(mode)
            "storage" -> scanMediaFilesInStorage()
            "all" -> getAllTracks()
            else -> Either.Left(None())
        }
    }

    override fun getAlbumImagePath(albumID: Long): Either<None, String> {
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)
        val selection = MediaStore.Audio.Albums._ID + "=?"
        val args = arrayOf(albumID.toString())

        val cursor = context.contentResolver.query(uri, projection, selection, args, null)

        var albumPath: String? = null

        if (cursor != null) {
            if (cursor.moveToFirst()) albumPath =
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
        }

        cursor?.close()

        return if (albumPath == null) Left(None()) else Either.Right(albumPath)
    }

    override fun getCategory(): Either<None, List<CategoryMusicModel>> {

        val array = ArrayList<CategoryMusicModel>()

        val category = listOf(

            CategoryMusicModel(R.drawable.ic_songers, "Исполнители", 0),
            CategoryMusicModel(R.drawable.ic_guitar, "Жанры", 1),
            CategoryMusicModel(R.drawable.ic_albom, "Альбомы", 2),
            CategoryMusicModel(R.drawable.ic_play_list, "Плейлисты", 3),
            CategoryMusicModel(R.drawable.ic_like_for_list, "Избранное", 4)
        )
        array.addAll(category)
        return Either.Right(array)
    }


    override fun getAllFolder(): Either<None, List<AllFolderWithMusic>> {

        var result = ArrayList<AllFolderWithMusic>()

        val directories = LinkedHashMap<String, ArrayList<ModelTest>>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"

        val selectionArgs =
            arrayOf("%" + "/storage/usbdisk0" + "%", "%" + "/storage/usbdisk0" + "/%/%")
        val order = MediaStore.Audio.Media.DATE_MODIFIED + " DESC"
        var DOWNLOAD_FILE_DIR = "/storage/usbdisk0"
        //val selection= MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val cursor = context.getContentResolver().query(uri, null, selection, null, order)

        if (cursor != null) {

            cursor?.let {

                it.moveToFirst()
                val pathIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                do {
                    val path = it.getString(pathIndex)
                    val file = File(path)
                    if (!file.exists()) {
                        continue
                    }
                    val fileDir = file.getParent()
                    var songURL = it.getString(it.getColumnIndex(MediaStore.Audio.Media.DATA))
                    var songAuth = it.getString(it.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    var songName = it.getString(it.getColumnIndex(MediaStore.Audio.Media.TITLE))

                    if (directories.containsKey(fileDir)) {
                        var songs = directories.getValue(fileDir);

                        var song = ModelTest(songURL, songAuth, songName)

                        songs.add(song)

                        directories.put(fileDir, songs)
                    } else {
                        var song = ModelTest(songURL, songAuth, songName)

                        var songs = ArrayList<ModelTest>()
                        songs.add(song)

                        directories.put(fileDir, songs)
                    }
                } while (it.moveToNext())


                for (dir in directories) {
                    var dirInfo: AllFolderWithMusic = AllFolderWithMusic(dir.key, dir.value)

                    result.add(dirInfo)
                }
            }
        }
        return Either.Right(result)
    }

    override fun getFilesFromPath(
        path: String,
        showHiddenFiles: Boolean,
        onlyFolders: Boolean
    ): List<File> {
        val file = File(path)
        return file.listFiles()
            .filter { showHiddenFiles || !it.name.startsWith(".") }
            .filter { !onlyFolders || it.isDirectory }
            .toList()
    }


    fun convertFileSizeToMB(sizeInBytes: Long): Double {
        return (sizeInBytes.toDouble()) / (1024 * 1024)
    }

    fun createNewFile(
        fileName: String,
        path: String,
        callback: (result: Boolean, message: String) -> Unit
    ) {
        val fileAlreadyExists = File(path).listFiles().map { it.name }.contains(fileName)
        if (fileAlreadyExists) {
            callback(false, "'${fileName}' already exists.")
        } else {
            val file = File(path, fileName)
            try {
                val result = file.createNewFile()
                if (result) {
                    callback(result, "File '${fileName}' created successfully.")
                } else {
                    callback(result, "Unable to create file '${fileName}'.")
                }
            } catch (e: Exception) {
                callback(false, "Unable to create file. Please try again.")
                e.printStackTrace()
            }
        }
    }

    fun createNewFolder(
        folderName: String,
        path: String,
        callback: (result: Boolean, message: String) -> Unit
    ) {
        val folderAlreadyExists = File(path).listFiles().map { it.name }.contains(folderName)
        if (folderAlreadyExists) {
            callback(false, "'${folderName}' already exists.")
        } else {
            val file = File(path, folderName)
            try {
                val result = file.mkdir()
                if (result) {
                    callback(result, "Folder '${folderName}' created successfully.")
                } else {
                    callback(result, "Unable to create folder '${folderName}'.")
                }
            } catch (e: Exception) {
                callback(false, "Unable to create folder. Please try again.")
                e.printStackTrace()
            }
        }
    }

    fun deleteFile(path: String) {
        val file = File(path)
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun scanMediaFilesInStorage(): Either<None, List<Track>> {

        val array = ArrayList<Track>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        //перечень набора данных из таблицы
        val projection = arrayOf(
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media._ID
        )
        //выбор аудио файлов
        var selection = "${MediaStore.Audio.Media.DATA} LIKE '/storage/emul%'"

        var albumArt = File("")

        val cursor = context.contentResolver.query(uri, projection, selection, null, null)

        if (cursor != null) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val duration =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                        .toLong()
                val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

                val id: Long =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val contentUri =
                    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                try {
                    val picture =
                        context.contentResolver.loadThumbnail(contentUri, Size(500, 350), null)
                    albumArt = getAlbumArt(picture, id.toString())
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                array.add(
                    Track(
                        id,
                        title,
                        artist,
                        data,
                        duration,
                        album,
                        albumArt.toString()
                    )
                )
                cursor.moveToNext()
            }
            cursor.close()
        }
        return if (array.isEmpty()) {
            Either.Left(None())
        } else {
            Either.Right(array)
        }
    }

    private fun getAlbumArt(picture: Bitmap, title: String): File {
        return bitmapToFile(picture, "$title.png")!!
    }

    private fun bitmapToFile(
        bitmap: Bitmap,
        fileNameToSave: String
    ): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + fileNameToSave
            )
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }


}