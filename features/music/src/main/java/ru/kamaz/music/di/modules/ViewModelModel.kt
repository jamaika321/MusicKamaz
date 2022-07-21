package ru.kamaz.music.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.kamaz.music.view_models.fragments.MainListMusicViewModel
import ru.kamaz.music.view_models.fragments.MusicFragmentViewModel
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
}