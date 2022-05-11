package ru.kamaz.music.cache.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.kamaz.music.cache.db.dao.HistoryDao
import ru.kamaz.music.cache.db.dao.LikeMusicDao
import ru.kamaz.music.cache.db.dao.PlayListDao
import ru.kamaz.music.domain.FavoriteSongsEntity
import ru.kamaz.music.domain.HistorySongsEntity
import ru.kamaz.music.domain.PlayListEntity

@Database(entities = arrayOf(FavoriteSongsEntity::class, HistorySongsEntity::class,PlayListEntity::class), version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): LikeMusicDao
    abstract fun historySongsDao(): HistoryDao
    abstract fun playListDao(): PlayListDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {

            if (INSTANCE != null) return INSTANCE!!

            synchronized(this) {

                INSTANCE = Room
                    .databaseBuilder(context, AppDatabase::class.java, "MUSIC_DATABASE")
                    .fallbackToDestructiveMigration()
                    .build()

                return INSTANCE!!

            }
        }
    }
}