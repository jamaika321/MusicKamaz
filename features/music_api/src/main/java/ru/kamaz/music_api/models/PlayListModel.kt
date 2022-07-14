package ru.kamaz.music_api.models

import java.io.Serializable

data class PlayListModel(
    val id: Long,
    val title: String,
    val albumArt: String,
    val trackTitleList: List<String>,
    val trackDataList: List<String>,
    var selection: Boolean = false
    ): Serializable