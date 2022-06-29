package ru.kamaz.musickamaz



import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.di.modules.CacheModule
import ru.kamaz.musickamaz.di.components.AppComponent
import ru.kamaz.musickamaz.di.components.DaggerAppComponent


import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseDaggerComponent
import java.lang.IllegalArgumentException

class App : BaseApplication() {

    lateinit var appComponent: AppComponent
    override fun provideComponent(type: Class<out BaseDaggerComponent>): BaseDaggerComponent {
        return when(type){
            MusicComponent::class.java -> appComponent.createMusicComponent().create(CacheModule(this))

            else -> throw IllegalArgumentException("rere $type")
        }
    }

    override fun onCreate() {
        super.onCreate()
        initAppComponent()

    }

    private fun initAppComponent() {
        appComponent = DaggerAppComponent.builder().build()
    }
}