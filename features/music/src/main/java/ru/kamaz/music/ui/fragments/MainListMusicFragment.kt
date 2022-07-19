package ru.kamaz.music.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.StateFlow
import ru.kamaz.music.databinding.FragmentMainListMusicBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.ui.NavAction.OPEN_ADD_PLAY_LIST_DIALOG
import ru.kamaz.music.ui.NavAction.OPEN_MUSIC_FRAGMENT
import ru.kamaz.music.ui.producers.*
import ru.kamaz.music.ui.producers.ItemType.RV_ITEM_MUSIC_GENRES
import ru.kamaz.music.view_models.MainListMusicViewModel
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.base.recycler_view.RecyclerViewAdapter
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import ru.sir.presentation.extensions.launchOn
import ru.sir.presentation.extensions.launchWhenStarted
import ru.sir.presentation.navigation.UiAction


class MainListMusicFragment
    :
    BaseFragment<MainListMusicViewModel, FragmentMainListMusicBinding>(MainListMusicViewModel::class.java) {
    override fun inject(app: BaseApplication) {
        app.getComponent<MusicComponent>().inject(this)
    }

    private var mode = ListState.PLAYLIST

    companion object {
        const val RV_ITEM_MUSIC_ARTIST = 0
        const val RV_ITEM_MUSIC_GENRES = 1
        const val RV_ITEM_MUSIC_ALBUMS = 2
        const val RV_ITEM_MUSIC_PLAYLIST = 3
        const val RV_ITEM_MUSIC_FAVORITE = 4
        const val RV_ITEM = 5
        const val RV_ITEM_MUSIC_CATEGORY = 6
        const val RV_ITEM_MUSIC_FOLDER = 7
        const val RV_ITEM_MUSIC_PLAYLIST_ADD_NEW = 8
    }

    override fun initVars() {
        viewModel.service.launchWhenStarted(lifecycleScope){
            if (it == null) return@launchWhenStarted
            initServiceVars()
        }
        categoryItemClicked(RV_ITEM)
    }

    override fun onBackPressed() {
        Log.i("ReviewTest_OnBack", " Fragment: ")
        when(mode) {
            ListState.PLAYLIST -> {
                backToPlayer()
            }
            ListState.FOLDER -> {
                backToPlayer()
            }
            ListState.CATEGORY -> {
                backToPlayer()
            }
            ListState.CATPLAYLIST -> {
                categoryItemClicked(RV_ITEM_MUSIC_CATEGORY)
            }
            ListState.FOLDPLAYLIST -> {
                categoryItemClicked(RV_ITEM_MUSIC_FOLDER)
            }
        }
        super.onBackPressed()
    }

    private fun backToPlayer(){
        navigator.navigateTo(
            UiAction(
                OPEN_MUSIC_FRAGMENT
            )
        )
    }

    enum class ListState(val value: Int){
        PLAYLIST(0),
        CATEGORY(1),
        CATPLAYLIST(2),
        FOLDER(3),
        FOLDPLAYLIST(4)
    }

    private fun initServiceVars() {
        viewModel.lastMusicChanged.launchOn(lifecycleScope) {
            viewModel.lastMusic(it)
        }

        viewModel.allMusic.launchWhenStarted(lifecycleScope){
            Log.i("ReviewTest_Update", "${it.isEmpty()}: ")
            if (mode == ListState.PLAYLIST) categoryItemClicked(RV_ITEM)
        }

        viewModel.listPlayList.launchWhenStarted(lifecycleScope){
            if (mode == ListState.CATPLAYLIST) categoryItemClicked(RV_ITEM_MUSIC_PLAYLIST)
        }

        viewModel.serviceTracks.launchWhenStarted(lifecycleScope){
            viewModel.fillAllTracksList()
            viewModel.loadAllDBLists()
        }

        binding.rvAllMusic.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                viewModel.rvPosition.value += dy
                if (viewModel.rvPosition.value > 5) {
                    binding.search.visibility = View.INVISIBLE
                } else {
                    binding.search.visibility = View.VISIBLE
                }
            }
        })
    }


    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentMainListMusicBinding.inflate(inflater, container, false)

    override fun setListeners() {
        binding.sourceSelection.listMusic.setOnClickListener {
            categoryItemClicked(RV_ITEM)
        }
        binding.sourceSelection.folderMusic.setOnClickListener {
            categoryItemClicked(RV_ITEM_MUSIC_FOLDER)
        }
        binding.sourceSelection.categoryMusic.setOnClickListener {
            categoryItemClicked(RV_ITEM_MUSIC_CATEGORY)
        }
        binding.search.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                binding.search.setIconified(false)
                searchActive()
            }
        })
        super.setListeners()
    }

    private fun recyclerViewAdapter(
        items: StateFlow<List<RecyclerViewBaseDataModel>>,
        id: Int
    ): RecyclerViewAdapter<List<RecyclerViewBaseDataModel>> {
        return when (id) {
            0 -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicArtistViewHolder())
                    .build { it }
            }
            1 -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicGenresViewHolder())
                    .build { it }
            }
            2 -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicAlbumsViewHolder())
                    .build { it }
            }
            3 -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(AddTrackViewHolder())
                    .build { it }
            }
            4 -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicFavoriteViewHolder())
                    .build { it }
            }
            5 -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicListViewHolderProducer())
                    .build { it }
            }
            6 -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicCategoryViewHolder())
                    .build { it }
            }
            7 -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicFoldersViewHolder())
                    .build { it }
            }
            else -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicListViewHolderProducer())
                    .build { it }
            }
        }
    }

    private fun searchActive() {
        val searchView = binding.search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    fun addNewPlaylist(){
        navigator.navigateTo(OPEN_ADD_PLAY_LIST_DIALOG)
    }

    fun onTrackClicked(track: Track) {
        viewModel.onItemClick(track, track.data)
    }

    fun onLikeClicked(track: Track) {
        viewModel.onLikeClicked(track)
    }

    fun onOptionsItemClicked(position: Int, track: Track) {
        //TODO
//        Log.i("RVPosition", "${viewModel.rvPosition.value}")
//        TrackOptionFragment().show(childFragmentManager, "TrackOptionFragment")
    }

    override fun onStop() {
        super.onStop()
        binding.rvAllMusic.layoutManager = null
    }

    fun categoryItemClicked(id: Int) {
        when (id) {
            in 0..2 -> {
                viewModel.getCategoryList(id)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.categoryList, id)
                this.mode = ListState.CATPLAYLIST
            }
            3 -> {
                viewModel.getPlayLists()
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.listPlayList, id)
                this.mode = ListState.CATPLAYLIST
            }
            4 -> {
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.favoriteSongs, id)
                this.mode = ListState.CATPLAYLIST
            }
            5 -> {
                binding.rvAllMusic.layoutManager = LinearLayoutManager(context)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.allMusic, id)
                this.mode = ListState.PLAYLIST
            }
            6 -> {
                binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.categoryOfMusic, id)
                this.mode = ListState.CATEGORY
            }
            7 -> {
                binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.foldersMusic, id)
                this.mode = ListState.FOLDER
            }
            8 -> {
                //TODO
            }
        }
        viewModel.rvPosition.value = 0
    }


    fun getCategoryLists(category: String, playlist: List<Track>): List<Track> {
        val categoryList: MutableList<Track> = mutableListOf()
        playlist.forEach {
            if (it.artist == category && !categoryList.contains(it.artist)) {
                categoryList.add(it)
            }
        }
        return categoryList
    }

    fun onFolderClicked(data: String){
        viewModel.fillFolderPlaylist(data)
        binding.rvAllMusic.layoutManager = LinearLayoutManager(context)
        binding.rvAllMusic.adapter = RecyclerViewAdapter.Builder(this, viewModel.folderMusicPlaylist)
            .addProducer(MusicListViewHolderProducer())
            .build { it }
        this.mode = ListState.FOLDPLAYLIST
    }


}



