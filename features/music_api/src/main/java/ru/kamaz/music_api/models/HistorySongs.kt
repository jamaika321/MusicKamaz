package ru.kamaz.music_api.models

import java.io.Serializable

data class HistorySongs (
    val id: Int,
    val data: String,
    val timePlayed: Long,
    val source: String,
    val sourceName: String = "",
    val favorites: Boolean = false
    ): Serializable