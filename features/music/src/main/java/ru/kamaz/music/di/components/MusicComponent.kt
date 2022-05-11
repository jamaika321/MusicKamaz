package ru.kamaz.music.di.components

import dagger.Subcomponent
import ru.kamaz.music.di.modules.*
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.ui.FolderFragment
import ru.kamaz.music.ui.list_music.MainListMusicFragment
import ru.kamaz.music.ui.all_musiclist.TrackFragment
import ru.kamaz.music.ui.one_song_fragment.MusicFragment
import ru.kamaz.music.ui.bt.BtFragment
import ru.kamaz.music.ui.category.CategoryFragment
import ru.kamaz.music.ui.category.dialog.DialogAddPlaylistFragment
import ru.kamaz.music.ui.dialog_windows.DialogBtSettings
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
    fun inject(fragment: TrackFragment)
    fun inject(service: MusicService)
    fun inject(fragment: BtFragment)
    fun inject(fragment: MainListMusicFragment)
    fun inject(fragment: CategoryFragment)
    fun inject(fragment: FolderFragment)
    fun inject(fragment: DialogBtSettings)
    fun inject(fragment: DialogAddPlaylistFragment)


}