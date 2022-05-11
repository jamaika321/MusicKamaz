package ru.kamaz.musickamaz.di.components.modules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
@Module
class AppModule(private val context: Context) {
    @Provides
    fun provideContext(): Context = context

    @Provides
    fun provideApplication(context: Context): Application = context as Application
}