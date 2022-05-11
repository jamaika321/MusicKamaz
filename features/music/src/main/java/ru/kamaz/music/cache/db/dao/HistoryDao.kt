package ru.kamaz.music.cache.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kamaz.music.domain.HistorySongsEntity


@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: HistorySongsEntity)

    @Query("SELECT * FROM history_songs")
    fun  loadAll(): List<HistorySongsEntity>

    @Query("SELECT * FROM history_songs")
    fun getLastMusic(): HistorySongsEntity
}