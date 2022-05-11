package ru.kamaz.music.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import ru.kamaz.music.cache.MusicCacheImpl
import ru.kamaz.music.cache.SharedPrefsManager
import ru.kamaz.music.cache.db.AppDatabase
import ru.kamaz.music.data.MusicCache
import ru.sir.presentation.base.BaseApplication
import javax.inject.Singleton

@Module
class CacheModule(private val context: Context) {

    @Provides
    fun provideAppContext(): Context = context

    @Provides
    fun provideApplication(): Application = context as BaseApplication

    @Provides
    @Singleton
    fun provideRoomManager():AppDatabase = AppDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }


    @Provides
    @Singleton
    fun provideCache(prefsManager: SharedPrefsManager, db : AppDatabase): MusicCache {
        return MusicCacheImpl(prefsManager, db)
    }
}