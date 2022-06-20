package ru.kamaz.music.cache.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kamaz.music.domain.HistorySongsEntity
import ru.kamaz.music.domain.TrackEntity
import ru.kamaz.music_api.models.Track


@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: HistorySongsEntity)

    @Query("SELECT * FROM history_songs")
    fun loadAll(): List<HistorySongsEntity>

    @Query("SELECT * FROM history_songs")
    fun getLastMusic(): HistorySongsEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrackList(tracks: List<TrackEntity>)

    @Query("SELECT * FROM track_list")
    fun getTrackList(): List<TrackEntity>


}