package ru.kamaz.music_api.models

import java.io.Serializable

data class PlayListModel(
    val id: Long,
    var title: String,
    var albumArt: String,
    val trackDataList: ArrayList<String>,
    var selection: Boolean = false
    ): Serializable