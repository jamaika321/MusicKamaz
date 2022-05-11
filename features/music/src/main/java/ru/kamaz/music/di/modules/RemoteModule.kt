package ru.kamaz.music.di.modules

import dagger.Module
import dagger.Provides
import ru.kamaz.music.data.Remote
import ru.kamaz.music.remote.RemoteImpl
import javax.inject.Singleton

@Module
class RemoteModule {
    @Provides
    @Singleton
    fun provideRemote(): Remote = RemoteImpl()
}