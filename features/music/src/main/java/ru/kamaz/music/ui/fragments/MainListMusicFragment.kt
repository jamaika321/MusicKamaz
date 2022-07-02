package ru.kamaz.music.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.kamaz.music.R
import ru.kamaz.music.databinding.FragmentMainListMusicBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.domain.GlobalConstants
import ru.kamaz.music.ui.NavAction
import ru.kamaz.music.ui.enums.PlayListFlow
import ru.kamaz.music.ui.getTypedSerializable
import ru.kamaz.music.view_models.MainListMusicViewModel
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.navigation.UiAction


class MainListMusicFragment
    :BaseFragment<MainListMusicViewModel, FragmentMainListMusicBinding>(MainListMusicViewModel::class.java) {
    override fun inject(app: BaseApplication) {
        app.getComponent<MusicComponent>().inject(this)
    }

    private val main: PlayListFlow by lazy {
        arguments?.getTypedSerializable( GlobalConstants.MAIN) ?: PlayListFlow.MAIN_WINDOW
    }



    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentMainListMusicBinding.inflate(inflater, container, false)

    override fun initVars() {
        startListAllMusic()


    }

    override fun setListeners() {
        binding.sourceSelection.listMusic.setOnClickListener {
           startListAllMusic()
        }
        binding.sourceSelection.folderMusic.setOnClickListener {
            startFolderListFragment()
        }
        binding.sourceSelection.categoryMusic.setOnClickListener {
           startCategoryMusic()
        }
        super.setListeners()
    }

    private fun startCategoryMusic(){
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, CategoryFragment())
            .commit()
    }

    private fun  startListAllMusic(){
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, TrackFragment())
            .commit()
    }

    private fun startFolderListFragment(){
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, FolderFragment())
            .commit()
    }


}



