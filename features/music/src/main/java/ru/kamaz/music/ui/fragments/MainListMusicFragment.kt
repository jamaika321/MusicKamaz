package ru.kamaz.music.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.flow.StateFlow
import ru.kamaz.music.databinding.FragmentMainListMusicBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.domain.GlobalConstants
import ru.kamaz.music.ui.enums.PlayListFlow
import ru.kamaz.music.ui.getTypedSerializable
import ru.kamaz.music.ui.producers.MusicCategoryViewHolder
import ru.kamaz.music.ui.producers.MusicFoldersViewHolder
import ru.kamaz.music.ui.producers.MusicListViewHolderProducer
import ru.kamaz.music.view_models.MainListMusicViewModel
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.base.recycler_view.RecyclerViewAdapter
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import ru.sir.presentation.extensions.launchWhenStarted


class MainListMusicFragment
    :BaseFragment<MainListMusicViewModel, FragmentMainListMusicBinding>(MainListMusicViewModel::class.java) {
    override fun inject(app: BaseApplication) {
        app.getComponent<MusicComponent>().inject(this)
    }

    private val main: PlayListFlow by lazy {
        arguments?.getTypedSerializable( GlobalConstants.MAIN) ?: PlayListFlow.MAIN_WINDOW
    }


    private fun initServiceVars(){
        viewModel.lastMusicChanged.launchWhenStarted(lifecycleScope){
            viewModel.lastMusic(it)
        }
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
        binding.playlistFragment.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
        binding.playlistFragment.rvAllMusic.adapter = recyclerViewCategoryAdapter()
    }

    private fun startListAllMusic(){
        binding.playlistFragment.rvAllMusic.adapter = recyclerViewPlaylistAdapter(viewModel.allMusic)
    }

    private fun startFolderListFragment(){
        binding.playlistFragment.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
        binding.playlistFragment.rvAllMusic.adapter = recyclerViewFolderMusic()
    }

    private fun recyclerViewPlaylistAdapter(items : StateFlow<List<RecyclerViewBaseDataModel>>) = RecyclerViewAdapter.Builder(this, items)
        .addProducer(MusicListViewHolderProducer())
        .build { it }

    private fun recyclerViewCategoryAdapter() = RecyclerViewAdapter.Builder(this, viewModel.categoryOfMusic)
        .addProducer(MusicCategoryViewHolder())
        .build { it }

    private fun recyclerViewFolderMusic() = RecyclerViewAdapter.Builder(this, viewModel.foldersMusic)
        .addProducer(MusicFoldersViewHolder())
        .build { it }


    private fun searchActive(){
        val searchView = binding.playlistFragment.search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener
        {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(musicText: String): Boolean {
                viewModel.searchMusic(musicText)
                return false
            }
        }
        )
    }

    fun onTrackClicked(track: Track) {
        viewModel.onItemClick(track , track.data)
    }

    fun onLikeClicked(track: Track) {
        viewModel.onLikeClicked(track)
    }

    fun onOptionsItemClicked(position: Int, track: Track) {
        //TODO
//        Log.i("RVPosition", "${viewModel.rvPosition.value}")
//        TrackOptionFragment().show(childFragmentManager, "TrackOptionFragment")
    }

}



