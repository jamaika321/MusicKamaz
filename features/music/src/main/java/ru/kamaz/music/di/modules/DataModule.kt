package ru.kamaz.music.di.modules

import android.content.Context
import android.media.MediaPlayer
import dagger.Module
import dagger.Provides
import ru.kamaz.music.data.MediaManager
import ru.kamaz.music.data.MusicCache
import ru.kamaz.music.data.RepositoryImpl
import ru.kamaz.music.media.AppMediaManager
import ru.kamaz.music_api.interfaces.Repository
import javax.inject.Singleton

@Module
class DataModule {
    @Provides
    fun provideMediaManager(context: Context): MediaManager {
        return AppMediaManager(context)
    }

    @Provides
    @Singleton
    fun provideMediaPlayer(): MediaPlayer = MediaPlayer()


    @Provides
    fun provideRepository(media: MediaManager, mediaPlayer: MediaPlayer,musicCache:MusicCache): Repository = RepositoryImpl(media, mediaPlayer,musicCache)
}