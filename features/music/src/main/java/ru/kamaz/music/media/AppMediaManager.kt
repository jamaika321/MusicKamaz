package ru.kamaz.music.media

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    override fun getMediaFilesFromPath(path: String, mode: String): Either<None, List<Track>> {
        Log.i("ReviewTest_GetMedia", "getMediaFilesFromPath: $path ")
        return when (path) {
            "sdCard" -> scanMediaFilesInSdCard(mode)
            "storage" -> scanMediaFilesInStorage(mode)
            "all" -> getAllTracks(mode)
            else -> Either.Left(None())
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun scanMediaFilesInSdCard(mode: String): Either<None, List<Track>> {
        metaRetriver = MediaMetadataRetriever()

        var listWithTrackData = ArrayList<Track>()
        val trackPaths = scanTracksPath()

        if (trackPaths is Either.Right) {
            listWithTrackData = when (mode) {
                "all" -> metaDataRetriver(trackPaths.r.size, trackPaths.r)
                "5" -> metaDataRetriver(trackPaths.r.size/2, trackPaths.r)
                else -> metaDataRetriver(1, trackPaths.r)
            }
        }
        //TODO
        //Переделать количество загружаемых треков

        return if (listWithTrackData.isEmpty()) {
            Either.Left(None())
        }else{
            Either.Right(listWithTrackData)
        }

    }

    private fun metaDataRetriver (cycleNum: Int, trackPaths: List<String>): ArrayList<Track> {
        val listWithTrackData = ArrayList<Track>()
        for (i in 0 until cycleNum) {
            metaRetriver.setDataSource(trackPaths[i])

            val artist =
                metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ARTIST))
                    ?: ("unknown")
            val album =
                metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ALBUM))
                    ?: ("unknown")
            val title =
                metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_TITLE))
                    ?: ("unknown")
            val genre =
                metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_GENRE))
                    ?: ("unknown")
            val duration =
                metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_DURATION))
                    ?.toLong() ?: (180)
            val data = trackPaths[i]
            val id = i.toLong()

            var albumArt = File("")

            var file : File? = null
            file = File(
                Environment.getExternalStorageDirectory().toString() + File.separator + "musicAlbumArt" + File.separator + title.replace("/", "")
            )
            if (!file.exists()) {
                var art = metaRetriver.embeddedPicture
                if (art != null) {
                    val bitMap = BitmapFactory.decodeByteArray(art, 0, art.size)
                    albumArt = getAlbumArt(bitMap, title.replace("/",""))
                    Log.i("ReviewTest_Embedded", " i am saved: $albumArt ")
                }
            } else {
                albumArt = file
            }



            listWithTrackData.add(
                Track(
                    id,
                    title,
                    artist,
                    data,
                    genre,
                    duration,
                    album,
                    albumArt.toString(),
                    source = "usb"
                )
            )
        }
        return listWithTrackData
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAllTracks(mode: String): Either<None, List<Track>> {
        val allTracks = ArrayList<Track>()

        var sdCard = scanMediaFilesInStorage(mode)
        if (sdCard is Either.Right){
            allTracks.addAll(sdCard.r)
        }
        sdCard = scanMediaFilesInSdCard(mode)
        if (sdCard is Either.Right){
            allTracks.addAll(sdCard.r)
        }
        return Either.Right(allTracks)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun scanMediaFilesInStorage(mode: String): Either<None, List<Track>> {

        val array = ArrayList<Track>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        //перечень набора данных из таблицы
        val projection = arrayOf(
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
//            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media._ID
        )
        //выбор аудио файлов
        var selection = "${MediaStore.Audio.Media.DATA} LIKE '/storage/emul%'"

        val cursor = context.contentResolver.query(uri, projection, selection, null, null)

        if (cursor != null) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                var albumArt = File("")
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val genre = ""
                val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)).toLong()
                val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val id = 5000 + cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                val pictureId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, pictureId)

                try {
                    val picture =
                        context.contentResolver.loadThumbnail(contentUri, Size(500, 350), null)
                    albumArt = getAlbumArt(picture, title.replace("/",""))
                } catch (e: IOException) {
                    e.printStackTrace()
                }


                array.add(
                    Track(
                        id,
                        title,
                        artist,
                        data,
                        genre,
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
    ): File? {
        var file: File? = null
        return try {

            file = File(
                Environment.getExternalStorageDirectory().toString() + File.separator + "musicAlbumArt" + File.separator
            )
            file.mkdirs()


            file = File(
                Environment.getExternalStorageDirectory().toString() + File.separator + "musicAlbumArt" + File.separator + fileNameToSave
            )
            if (!file.exists()) file.createNewFile()


            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
            val bitmapdata = bos.toByteArray()


            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file
        }
    }

    override fun deleteAlbumArtDir(){
        var file : File? = null
        try {
            file = File(
                Environment.getExternalStorageDirectory().toString() + File.separator + "musicAlbumArt" + File.separator
            )
            file.deleteRecursively()
        } catch (e: Exception){
            e.printStackTrace()
        }
    }


}