package ru.kamaz.music.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_list")
data class PlayListEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var idPlayList: Long,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "albumArt")
    var albumArt : String
)
