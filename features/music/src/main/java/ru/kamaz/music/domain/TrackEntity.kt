package ru.kamaz.music.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_list")
class TrackEntity (

    @ColumnInfo(name = "track_id")
    val id: Long,
    @PrimaryKey
    val title: String,
    val artist: String,
    val data: String,
    val duration: Long,
    val album: String,
    val albumArt: String,
    var playing: Boolean = false
        )