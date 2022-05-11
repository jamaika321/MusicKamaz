package ru.kamaz.music_api.models

import ru.kamaz.music_api.FileType

data class FileModel(
    val path: String,
    val tupe: FileType,
    val name: String,
    val sizeInMB: Double,
    val extension: String = "",
    val subFiles: Int = 0
)