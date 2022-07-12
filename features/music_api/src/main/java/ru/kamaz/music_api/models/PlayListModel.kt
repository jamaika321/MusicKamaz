package ru.kamaz.music_api.models

data class PlayListModel(
    val id: Long,
    val title: String,
    val albumArt: String,
    var selection: Boolean = false
    )