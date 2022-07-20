package ru.kamaz.music.cache.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.kamaz.music.domain.FavoriteSongsEntity
import ru.kamaz.music.domain.TrackEntity

@Dao
interface LikeMusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: FavoriteSongsEntity)

    @Delete
    fun delete(user: FavoriteSongsEntity)

    @Query("SELECT * FROM like_songs WHERE data=:data")
    fun loadAll(data:String): FavoriteSongsEntity

    @Query("SELECT * FROM like_songs ")
    fun getData(): Flow<List<FavoriteSongsEntity>>

}