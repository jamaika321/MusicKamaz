package ru.kamaz.music.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.kamaz.music.view_models.*
import ru.kamaz.music.view_models.bt.BtDialogFragmentViewModel
import ru.kamaz.music.view_models.bt.BtFragmentViewModel
import ru.sir.presentation.annotations.ViewModelKey
import ru.sir.presentation.factories.ViewModelFactory

@Module
abstract class ViewModelModel() {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MusicFragmentViewModel::class)
    abstract fun bindMusicViewModel(model: MusicFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainListMusicViewModel::class)
    abstract fun bindMainListMusicViewModel(model: MainListMusicViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BtFragmentViewModel::class)
    abstract fun bindBtViewModel(model: BtFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DialogViewModel::class)
    abstract fun bindDialogViewModel(model: DialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BtDialogFragmentViewModel::class)
    abstract fun bindBtDialogFragmentViewModel(model: BtDialogFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DialogAddPlaylistFragmentViewModel::class)
    abstract fun bindDialogAddPlaylistFragmentViewModel(model: DialogAddPlaylistFragmentViewModel): ViewModel

    /*  @Binds
     @IntoMap
     @ViewModelKey(MusicCategoryViewModel::class)
     abstract fun bindMusicCategoryViewModel(model: MusicCategoryViewModel): ViewModel
 */

}