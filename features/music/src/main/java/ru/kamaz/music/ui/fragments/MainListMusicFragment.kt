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
import ru.kamaz.music.ui.NavAction.OPEN_MUSIC_FRAGMENT
import ru.kamaz.music.ui.producers.*
import ru.kamaz.music.ui.producers.ItemType.RV_ITEM_MUSIC_GENRES
import ru.kamaz.music.view_models.MainListMusicViewModel
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.base.recycler_view.RecyclerViewAdapter
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
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
//        categoryItemClicked(RV_ITEM)
        initServiceVars()
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
        viewModel.lastMusicChanged.launchWhenStarted(lifecycleScope) {
            viewModel.lastMusic(it)
        }

        viewModel.allMusic.launchWhenStarted(lifecycleScope){
            if (mode == ListState.PLAYLIST) categoryItemClicked(RV_ITEM)
        }

        setFragmentResultListener("lastMusic") { key, bundle ->
            val result = bundle.getString("bundleKey")
            if (!result.isNullOrEmpty()) viewModel.lastMusic.value = result
        }

        binding.rvAllMusic.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                viewModel.rvPosition.value += dy
                if (viewModel.rvPosition.value > 5) {
                    binding.search.visibility = View.INVISIBLE
                } else {
                    binding.search.visibility = View.VISIBLE
                }
                super.onScrolled(recyclerView, dx, dy)
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
                    .addProducer(MusicPlayListViewHolder())
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
                viewModel._categoryList.value =
                    viewModel.loadingMusic.value.toRecyclerViewItemOfList(id)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.categoryList, id)
                this.mode = ListState.CATPLAYLIST
            }
            3 -> {
                //TODO
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

    private fun List<Track>.toRecyclerViewItemOfList(id: Int): List<RecyclerViewBaseDataModel> {
        var listType = RV_ITEM
        when (id) {
            0 -> listType = RV_ITEM_MUSIC_ARTIST
            1 -> listType = RV_ITEM_MUSIC_GENRES
            2 -> listType = RV_ITEM_MUSIC_ALBUMS
            3 -> listType = RV_ITEM_MUSIC_PLAYLIST
            4 -> listType = RV_ITEM_MUSIC_FAVORITE
            5 -> listType = RV_ITEM
            6 -> listType = RV_ITEM_MUSIC_CATEGORY
            7 -> listType = RV_ITEM_MUSIC_FOLDER
            8 -> listType = RV_ITEM_MUSIC_PLAYLIST_ADD_NEW
        }
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach {
            newList.add(
                RecyclerViewBaseDataModel(
                    it,
                    listType
                )
            )
        }
        return newList
    }


}



