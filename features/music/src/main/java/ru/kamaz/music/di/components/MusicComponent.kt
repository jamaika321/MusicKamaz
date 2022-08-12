package ru.kamaz.music.di.components

import dagger.Subcomponent
import ru.kamaz.music.di.modules.*
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music.ui.fragments.MusicFragment
import ru.kamaz.music.ui.fragmentDialog.DialogAddPlaylistFragment
import ru.kamaz.music.ui.fragmentDialog.DialogAddTrack
import ru.sir.presentation.base.BaseDaggerComponent
import javax.inject.Singleton

@Singleton
@Subcomponent(modules = [ViewModelModel::class, CacheModule::class,DataModule::class,DomainModule::class])
interface MusicComponent : BaseDaggerComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(cacheModule: CacheModule): MusicComponent
    }

    fun inject(fragment: MusicFragment)
    fun inject(service: MusicService)
    fun inject(fragment: MainListMusicFragment)
    fun inject(fragment: DialogAddPlaylistFragment)
    fun inject(fragment: DialogAddTrack)


}