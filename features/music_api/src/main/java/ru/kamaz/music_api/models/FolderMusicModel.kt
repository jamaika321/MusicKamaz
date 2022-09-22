package ru.kamaz.music_api.models

data class FolderMusicModel(
    val id: Long,
    val title: String,
    val artist: String,
    val data: String,
    val duration: String,
    val albumId: Long,
    val fileType: Int
)