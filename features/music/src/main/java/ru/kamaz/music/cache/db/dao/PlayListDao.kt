package ru.kamaz.music.cache.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.kamaz.music.domain.PlayListEntity


@Dao
interface PlayListDao  {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(playList: PlayListEntity)

    @Delete
    fun delete(playList: PlayListEntity)

    @Query("SELECT * FROM play_list ")
    fun getData(): List<PlayListEntity>

}
