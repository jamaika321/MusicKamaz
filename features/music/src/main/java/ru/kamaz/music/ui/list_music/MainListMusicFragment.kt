package ru.kamaz.music.ui.list_music

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import ru.kamaz.music.R
import ru.kamaz.music.databinding.FragmentMainListMusicBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.domain.GlobalConstants
import ru.kamaz.music.ui.FolderFragment
import ru.kamaz.music.ui.all_musiclist.TrackFragment
import ru.kamaz.music.ui.category.CategoryFragment
import ru.kamaz.music.ui.enums.PlayListFlow
import ru.kamaz.music.ui.getTypedSerializable
import ru.kamaz.music.view_models.MainListMusicViewModel
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.extensions.launchWhenStarted


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
//        Toast.makeText(context, arguments?.get("source").toString(), Toast.LENGTH_SHORT).show()
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
        parentFragmentManager.beginTransaction().replace(R.id.fragment, CategoryFragment()).addToBackStack("CategoryFragment").commit()
    }

    private fun  startListAllMusic(){
        val trackFrag = TrackFragment()
        setFragmentResultListener("sourceEnum") { key , bundle ->
            when (bundle.getInt("bundleKey")){
                2 -> trackFrag.arguments = bundleOf("bundleKey" to "2")
                3 -> trackFrag.arguments = bundleOf("bundleKey" to "3")
            }
        }
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, trackFrag)
            .addToBackStack("TrackFragment")
            .commit()
    }

    private fun startFolderListFragment(){
        parentFragmentManager.beginTransaction().replace(R.id.fragment, FolderFragment()).addToBackStack("FolderFragment").commit()
    }


}



