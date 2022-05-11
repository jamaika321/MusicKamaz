package ru.kamaz.musickamaz.di.components

import dagger.Component
import ru.kamaz.music.di.components.MusicComponent


@Component
interface AppComponent  {
    fun createMusicComponent(): MusicComponent.Factory
}