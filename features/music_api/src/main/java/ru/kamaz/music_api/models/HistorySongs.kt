package ru.kamaz.music_api.models

import java.io.Serializable

data class HistorySongs (
    val dbID:Int,
    val idCursor: Int,
    val title: String,
    val trackNumber: Int,
    val year: Int,
    val duration: Long,
    val data: String,
    val dateModified: Long,
    val albumArt: String,
    val albumName: String,
    val artistId: Long,
    val artistName: String,
    val albumArtist: String?,
    val timePlayed: Long
    ): Serializable