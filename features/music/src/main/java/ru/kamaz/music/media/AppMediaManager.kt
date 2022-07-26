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


class AppMediaManager @Inject constructor(val context: Context) : MediaManager {

    private lateinit var metaRetriver: MediaMetadataRetriever

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getMediaFilesFromPath(path: String, mode: String): Either<None, List<Track>> {
        return when (path) {
            "sdCard" -> scanMediaFilesInSdCard(mode)
            "storage" -> getAllTracks(mode)
            "all" -> getAllTracks(mode)
            else -> Either.Left(None())
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun scanMediaFilesInSdCard(mode: String): Either<None, List<Track>> {

        var listWithTrackData = ArrayList<Track>()
        val trackPaths = scanTracksPath("usb")

        if (trackPaths is Either.Right) {
            listWithTrackData = when (mode) {
                "all" -> metaDataRetriver(trackPaths.r.size, trackPaths.r)
                "5" -> metaDataRetriver(5, trackPaths.r)
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

    private fun metaDataRetriver(cycleNum: Int, trackPaths: List<String>): ArrayList<Track> {
        val listWithTrackData = ArrayList<Track>()
        metaRetriver = MediaMetadataRetriever()
        for (i in 0 until cycleNum) {
            metaRetriver.setDataSource(trackPaths[i])

            val artist =
                metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ARTIST)) + i.toString()
                    ?: ("unknown")
            val album =
                metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ALBUM))
                    ?: ("unknown")
            val title = metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_TITLE)) ?: ("unknown")
            val duration =
                metaRetriver.extractMetadata((MediaMetadataRetriever.METADATA_KEY_DURATION))
                    ?.toLong() ?: (180)
            val data = trackPaths[i]
            val id = i.toLong()
            val source = if (data.contains("/storage/usb")){
                    "usb"
            } else {
                "disk"
            }

            var albumArt = File("")

            val file = File(
                Environment.getExternalStorageDirectory().toString() + File.separator + "musicAlbumArt" + File.separator + title.replace("/", "") + ".png"
            )
            if (!file.exists()) {
                var art = metaRetriver.embeddedPicture
                if (art != null) {
                    val bitMap = BitmapFactory.decodeByteArray(art, 0, art.size)
                    albumArt = getAlbumArt(bitMap, title.replace("/",""))
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
                    duration,
                    album,
                    albumArt.toString(),
                    false,
                    false,
                    source
                )
            )
        }
        return listWithTrackData
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAllTracks(mode: String): Either<None, List<Track>> {
        var allTracks = ArrayList<Track>()
        val allPath = scanTracksPath("disk")

        if (allPath is Either.Right) {
            allTracks = when (mode) {
                "all" -> metaDataRetriver(allPath.r.size, allPath.r)
                "5" -> metaDataRetriver(5, allPath.r)
                else -> metaDataRetriver(1, allPath.r)
            }
        }
        return Either.Right(allTracks)
    }



    private fun scanTracksPath(source: String): Either<None, List<String>> {
        val list = ArrayList<String>()
        when (source) {
            "usb" -> {
                list.addAll(readRecursive(File("/storage/usbdisk0"), listOf("mp3", "wav")).sorted().map {
                    it.toString()
                })
            }
            "disk" -> {
                list.addAll(readRecursive(File("/storage/emulated/0"), listOf("mp3", "wav")).sorted().map {
                    it.toString()
                })
                list.addAll(readRecursive(File("/storage/usbdisk0"), listOf("mp3", "wav")).sorted().map {
                    it.toString()
                })
            }
        }

        return if (list.isEmpty()) {
            Either.Left(None())
        } else {
            Either.Right(list)
        }
    }

    private fun readRecursive(root: File, extentions: List<String>): ArrayList<File> {
        val list = ArrayList<File>()
        root.listFiles()?.filter { it.isDirectory }?.forEach {
            list.addAll(readRecursive(it, extentions))
        }

        root.listFiles()?.filter { extentions.contains(it.extension) }?.forEach {
            list.add(it)
        }
        return list
    }

    private fun getFolderWithMusic(root: File, extensions: List<String>): List<File> {
        var list = ArrayList<File>()
        root.listFiles()?.filter { it.isDirectory }?.forEach {
            list.addAll(getFolderWithMusic(it, extensions))
        }

        root.listFiles()?.filter { extensions.contains(it.extension) }?.forEach {
            if (it != null){
                list.add(root)
                Log.i("ReviewTest_Get", " : ${root} ")
            }
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
            CategoryMusicModel(R.drawable.ic_albom, "Альбомы", 2),
            CategoryMusicModel(R.drawable.ic_play_list, "Плейлисты", 3),
            CategoryMusicModel(R.drawable.ic_like_for_list, "Избранное", 4)
        )
        array.addAll(category)
        return Either.Right(array)
    }


    override fun getAllFolder(): Either<None, List<AllFolderWithMusic>> {

        var result = ArrayList<AllFolderWithMusic>()

        getFolderWithMusic(File("/storage/usbdisk0"), listOf("mp3", "wav")).forEach {
            if (!result.contains(AllFolderWithMusic(it.name, it.toString()))) result.add(AllFolderWithMusic(it.name, it.toString()))
        }
        getFolderWithMusic(File("/storage/emulated/0"), listOf("mp3", "wav")).forEach {
            if (!result.contains(AllFolderWithMusic(it.name, it.toString()))) result.add(AllFolderWithMusic(it.name, it.toString()))
        }

        return Either.Right(result)
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