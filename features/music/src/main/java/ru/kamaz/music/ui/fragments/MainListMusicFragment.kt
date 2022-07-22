package ru.kamaz.music.ui.fragments

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.StateFlow
import ru.kamaz.music.R
import ru.kamaz.music.databinding.FragmentMainListMusicBinding
import ru.kamaz.music.databinding.PlaylistContextMenuBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.ui.NavAction.OPEN_ADD_PLAY_LIST_DIALOG
import ru.kamaz.music.ui.NavAction.OPEN_MUSIC_FRAGMENT
import ru.kamaz.music.ui.fragmentDialog.TrackOptionFragment
import ru.kamaz.music.ui.producers.*
import ru.kamaz.music.view_models.fragments.MainListMusicViewModel
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
        const val RV_ITEM_MUSIC_ALBUMS = 2
        const val RV_ITEM_MUSIC_PLAYLIST = 3
        const val RV_ITEM_MUSIC_FAVORITE = 4
        const val RV_ITEM = 5
        const val RV_ITEM_MUSIC_CATEGORY = 6
        const val RV_ITEM_MUSIC_FOLDER = 7
        const val RV_ITEM_PLAYLIST = 8
    }

    override fun initVars() {
        viewModel.service.launchWhenStarted(lifecycleScope){
            if (it == null) return@launchWhenStarted
            initServiceVars()
        }
        categoryItemClicked(RV_ITEM)
    }

    override fun onBackPressed() {
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
            ListState.FOLDPLAYLIST -> {
                categoryItemClicked(RV_ITEM_MUSIC_FOLDER)
            }
            ListState.PLAYLISTMUSIC -> {
                categoryItemClicked(RV_ITEM_MUSIC_PLAYLIST)
            }
            else -> {
                categoryItemClicked(RV_ITEM_MUSIC_CATEGORY)
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
        FOLDPLAYLIST(4),
        PLAYLISTMUSIC(5),
        CATFAVORITES(6),
        CATARTIST(7)
    }

    private fun initServiceVars() {
        viewModel.lastMusicChanged.launchOn(lifecycleScope) {
            viewModel.lastMusic(it)
        }

        viewModel.allMusic.launchWhenStarted(lifecycleScope){
            if (mode == ListState.PLAYLIST) categoryItemClicked(RV_ITEM)
        }

        viewModel.listPlayList.launchWhenStarted(lifecycleScope){
            if (mode == ListState.CATPLAYLIST) categoryItemClicked(RV_ITEM_MUSIC_PLAYLIST)
        }

        viewModel.serviceTracks.launchWhenStarted(lifecycleScope){
            viewModel.fillAllTracksList()
            viewModel.loadAllDBLists()
        }

        viewModel.foldersMusic.launchWhenStarted(lifecycleScope){
            if (mode == ListState.FOLDER) categoryItemClicked(RV_ITEM_MUSIC_FOLDER)
        }

        viewModel.favoriteSongs.launchWhenStarted(lifecycleScope){
            if (mode == ListState.CATFAVORITES) categoryItemClicked(RV_ITEM_MUSIC_FAVORITE)
        }

        binding.rvAllMusic.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
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
            8 -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicListViewHolderProducer())
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

    fun onTrackClicked(track: Track, position: Int) {
        viewModel.rvPosition.value = position
        viewModel.onItemClick(track, track.data)
    }

    fun onLikeClicked(track: Track) {
        viewModel.onLikeClicked(track)
    }

    fun onOptionsItemClicked(position: Int, track: Track) {
        TrackOptionFragment().show(childFragmentManager, "TrackOptionFragment")
    }

    override fun onStop() {
        super.onStop()
        binding.rvAllMusic.layoutManager = null
    }

    fun categoryItemClicked(id: Int) {
        when (id) {
            0 -> {
                viewModel.getCategoryList(id)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.categoryList, id)
                this.mode = ListState.CATARTIST
            }
            2 -> {
                viewModel.getCategoryList(id)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.categoryList, id)
                this.mode = ListState.CATPLAYLIST
            }
            3 -> {
                binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.listPlayList, id)
                this.mode = ListState.CATPLAYLIST
            }
            4 -> {
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.favoriteSongs, id)
                this.mode = ListState.CATFAVORITES
            }
            5 -> {
                binding.rvAllMusic.layoutManager = LinearLayoutManager(context)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.allMusic, id)
                binding.rvAllMusic.scrollToPosition(viewModel.rvPosition.value)
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
                binding.rvAllMusic.layoutManager = LinearLayoutManager(context)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.playListMusic, id)
                this.mode = ListState.PLAYLISTMUSIC
            }
        }
//        viewModel.rvPosition.value = 0
    }

    fun playListSelected(name: String){
        val trackList = ArrayList<Track>()
        viewModel.notFLowList.forEach { playList ->
            if (playList.title == name){
                playList.trackDataList.forEach { data ->
                    viewModel.serviceTracks.value.forEach { tracks ->
                        if (data == tracks.data){
                            trackList.add(tracks)
                        }
                    }
                }
            }
        }
        viewModel.getPlayListMusic(trackList)
        categoryItemClicked(RV_ITEM_PLAYLIST)
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

    fun deletePlayList(name: String){
        viewModel.deletePlaylist(name)
    }


}



