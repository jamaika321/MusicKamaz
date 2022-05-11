package ru.kamaz.music.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "like_songs")
data class FavoriteSongsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_song")
    var idSong: Int,
    @ColumnInfo(name = "data")
    var data: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "artist")
    val artist: String
)

