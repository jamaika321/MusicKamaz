package ru.kamaz.music.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_db")
class TestDBData(@PrimaryKey @ColumnInfo(name = "test") val test: String)