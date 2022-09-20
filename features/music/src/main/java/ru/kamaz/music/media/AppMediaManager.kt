package ru.kamaz.music.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import ru.kamaz.music.R
import ru.kamaz.music.data.MediaManager
import ru.kamaz.music_api.models.AllFolderWithMusic
import ru.kamaz.music_api.models.CategoryMusicModel
import ru.kamaz.music_api.models.Track
import ru.sir.core.Either
import ru.sir.core.Either.Left
import ru.sir.core.None
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


class AppMediaManager @Inject constructor(val context: Context) : MediaManager {

    private lateinit var metaRetriever: MediaMetadataRetriever

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getMediaFilesFromPath(path: String, mode: String): Either<None, List<Track>> {
        return when (path) {
            "sdCard" -> scanMediaFilesInSdCard(mode)
            "storage" -> getAllTracks(mode)
            "all" -> getAllTracks(mode)
            else -> Left(None())
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun scanMediaFilesInSdCard(mode: String): Either<None, List<Track>> {

        var listWithTrackData = ArrayList<Track>()
        val trackPaths = scanTracksPath("usb")

        if (trackPaths is Either.Right) {
            listWithTrackData = when (mode) {
                "all" -> metaDataRetriever(trackPaths.r.size, trackPaths.r)
                "5" -> metaDataRetriever(5, trackPaths.r)
                else -> metaDataRetriever(1, trackPaths.r)
            }
        }
        //TODO
        //Переделать количество загружаемых треков

        return if (listWithTrackData.isEmpty()) {
            Left(None())
        } else {
            Either.Right(listWithTrackData)
        }

    }

    override fun loadLastTrack(trackPaths: List<String>): Either<None, ArrayList<Track>> {
        return if (File(trackPaths[0]).exists()) Either.Right(metaDataRetriever(1, trackPaths))
        else Left(None())
    }

    private fun metaDataRetriever(cycleNum: Int, trackPaths: List<String>): ArrayList<Track> {
        val listWithTrackData = ArrayList<Track>()
        metaRetriever = MediaMetadataRetriever()
        for (i in 0 until cycleNum) {
            if (i == trackPaths.size) break
            metaRetriever.setDataSource(trackPaths[i])

            val artist =
                metaRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ARTIST))
                    ?: ("unknown")
            val album =
                metaRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ALBUM))
                    ?: ("unknown")
            val title =
                metaRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_TITLE))
                    ?: ("unknown")
            val duration =
                metaRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_DURATION))
                    ?.toLong() ?: (180)
            val data = trackPaths[i]
            val id = i.toLong()


            val source = if (data.contains("/storage/usb")) {
                "usb"
            } else {
                "disk"
            }


            var albumArt = File("")

            val file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "musicAlbumArt" + File.separator + title.replace(
                    "/",
                    ""
                ) + ".png"
            )
            if (!file.exists()) {
                val art = metaRetriever.embeddedPicture
                if (art != null) {
                    val bitMap = BitmapFactory.decodeByteArray(art, 0, art.size)
                    albumArt = getAlbumArt(bitMap, title.replace("/", ""))
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

    override fun deleteTrackFromMemory(data: String) {
        val file = File(data)
        file.delete()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAllTracks(mode: String): Either<None, List<Track>> {
        var allTracks = ArrayList<Track>()
        val allPath = scanTracksPath("disk")

        if (allPath is Either.Right) {
            allTracks = when (mode) {
                "all" -> metaDataRetriever(allPath.r.size, allPath.r)
                "5" -> metaDataRetriever(5, allPath.r)
                else -> metaDataRetriever(1, allPath.r)
            }
        }
        return Either.Right(allTracks)
    }


    private fun scanTracksPath(source: String): Either<None, List<String>> {
        val list = ArrayList<String>()
        when (source) {
            "usb" -> {
                list.addAll(
                    readRecursive(File("/storage/usbdisk0"), listOf("mp3", "wav")).sorted().map {
                        it.toString()
                    })
            }
            "disk" -> {
                list.addAll(
                    readRecursive(
                        File("/storage/emulated/0"),
                        listOf("mp3", "wav")
                    ).sorted().map {
                        it.toString()
                    })
                list.addAll(
                    readRecursive(File("/storage/usbdisk0"), listOf("mp3", "wav")).sorted().map {
                        it.toString()
                    })
            }
        }

        return if (list.isEmpty()) {
            Left(None())
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
        val list = ArrayList<File>()
        root.listFiles()?.filter { it.isDirectory }?.forEach {
            list.addAll(getFolderWithMusic(it, extensions))
        }

        root.listFiles()?.filter { extensions.contains(it.extension) }?.forEach {
            if (it != null) {
                list.add(root)
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


    override fun getAllFolder(): Either<None, List<AllFolderWithMusic>> {

        val result = ArrayList<AllFolderWithMusic>()

        getFolderWithMusic(File("/storage/usbdisk0"), listOf("mp3", "wav")).forEach {
            if (!result.contains(AllFolderWithMusic(it.name, it.toString()))) result.add(
                AllFolderWithMusic(it.name, it.toString())
            )
        }
        getFolderWithMusic(File("/storage/emulated/0"), listOf("mp3", "wav")).forEach {
            if (!result.contains(AllFolderWithMusic(it.name, it.toString()))) result.add(
                AllFolderWithMusic(it.name, it.toString())
            )
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
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "musicAlbumArt" + File.separator
            )
            file.mkdirs()


            file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "musicAlbumArt" + File.separator + fileNameToSave
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

    override fun deleteAlbumArtDir() {
        var file: File?
        try {
            file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "musicAlbumArt" + File.separator
            )
            file.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}