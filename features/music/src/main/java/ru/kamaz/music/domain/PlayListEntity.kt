package ru.kamaz.music.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName

@Entity(tableName = "play_list")
data class PlayListEntity(
    @ColumnInfo(name = "id")
    var idPlayList: Long,
    @PrimaryKey
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "albumArt")
    var albumArt : String,
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "trackTitleList")
    var trackTitleList: List<String>,
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "trackDataList")
    var trackDataList: List<String>,
)
