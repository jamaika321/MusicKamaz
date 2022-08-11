package ru.kamaz.music.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity (tableName = "history_songs")
class HistorySongsEntity (
    @ColumnInfo(name = "id")
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "data")
    val data: String,
    @ColumnInfo(name = "time_played")
    val timePlayed: Long,
    @ColumnInfo(name = "source")
    val source: String,
    @ColumnInfo(name = "sourceName")
    val sourceName: String
    )