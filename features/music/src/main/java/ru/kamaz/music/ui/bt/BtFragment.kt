package ru.kamaz.music.ui.bt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.kamaz.music.databinding.BtFragmentBinding
import ru.kamaz.music.di.components.MusicComponent

import ru.kamaz.music.view_models.bt.BtFragmentViewModel
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment

class  BtFragment :   BaseFragment<BtFragmentViewModel, BtFragmentBinding>(BtFragmentViewModel::class.java) {
    override fun inject(app: BaseApplication) {
        app.getComponent<MusicComponent>().inject(this)
    }

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )= BtFragmentBinding.inflate(inflater, container, false)

}