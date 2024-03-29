package ru.kamaz.music_api.models

import java.io.Serializable

data class Track(
    var id: Long,
    val title: String,
    val artist: String,
    val data: String,
    val duration: Long,
    val album: String,
    val albumArt: String,
    var playing: Boolean = false,
    var favorite: Boolean = false,
    var source: String = "disk",
    var scrollPosition: Boolean = false
) : Serializable {
    companion object {
        fun convertDuration(value: Long): String {

            val hrs = value / 3600000
            val mns = value / 60000 % 60000
            val scs = value % 60000 / 1000

            return when (hrs) {
                0L -> String.format("%02d:%02d", mns, scs)
                else -> String.format("%02d:%02d:%02d", hrs, mns, scs)
            }
        }
    }

}