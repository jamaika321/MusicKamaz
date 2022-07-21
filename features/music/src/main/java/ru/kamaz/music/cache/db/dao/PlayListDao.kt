package ru.kamaz.music.cache.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.kamaz.music.domain.PlayListEntity


@Dao
interface PlayListDao  {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(playList: PlayListEntity)

    @Query("DELETE FROM play_list WHERE name = :playList")
    fun delete(playList: String)

    @Query("SELECT * FROM play_list ")
    fun getData(): Flow<List<PlayListEntity>>

    @Query("UPDATE play_list SET name = :name, trackTitleList = :title, trackDataList = :data WHERE name = :name")
    fun updatePlayList(name: String, title: List<String>, data: List<String>)

}
