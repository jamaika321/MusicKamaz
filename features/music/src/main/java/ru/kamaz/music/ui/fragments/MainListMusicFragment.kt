package ru.kamaz.music.ui.fragments

import android.os.Bundle
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
import ru.kamaz.music.ui.producers.MusicArtistViewHolder
import ru.kamaz.music.ui.producers.MusicCategoryViewHolder
import ru.kamaz.music.ui.producers.MusicFoldersViewHolder
import ru.kamaz.music.ui.producers.MusicListViewHolderProducer
import ru.kamaz.music.view_models.MainListMusicViewModel
import ru.kamaz.music.view_models.music_category.ItemArtist
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

    companion object {
        private const val RV_ITEM = 2
        private const val RV_ITEM_MUSIC_CATEGORY = 3
        private const val RV_ITEM_MUSIC_FOLDER = 5
    }

    override fun initVars() {
        startListAllMusic()
        initServiceVars()
    }

    private fun initServiceVars(){
        viewModel.lastMusicChanged.launchWhenStarted(lifecycleScope){
            viewModel.lastMusic(it)
            startListAllMusic()
        }

        setFragmentResultListener("lastMusic") { key, bundle ->
            val result = bundle.getString("bundleKey")
            if (!result.isNullOrEmpty())  viewModel.lastMusic.value = result
        }

        binding.rvAllMusic.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                viewModel.rvPosition.value += dy
                if (viewModel.rvPosition.value > 5){
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
            startListAllMusic()
        }
        binding.sourceSelection.folderMusic.setOnClickListener {
            startFolderListFragment()
        }
        binding.sourceSelection.categoryMusic.setOnClickListener {
            startCategoryMusic()
        }
        binding.search.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                binding.search.setIconified(false)
                searchActive()
            }
        })
        super.setListeners()
    }

    private fun startCategoryMusic(){
        binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
        binding.rvAllMusic.adapter = recyclerViewCategoryAdapter(viewModel.categoryOfMusic)
    }

    private fun startListAllMusic(){
        binding.rvAllMusic.layoutManager  = LinearLayoutManager(context)
        binding.rvAllMusic.adapter = recyclerViewPlaylistAdapter(viewModel.allMusic)
    }

    private fun startFolderListFragment(){
        binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
        binding.rvAllMusic.adapter = recyclerViewFolderMusic()
    }

    private fun recyclerViewPlaylistAdapter(items : StateFlow<List<RecyclerViewBaseDataModel>>) = RecyclerViewAdapter.Builder(this, items)
        .addProducer(MusicListViewHolderProducer())
        .build { it }

    private fun recyclerViewCategoryAdapter(items : StateFlow<List<RecyclerViewBaseDataModel>>) = RecyclerViewAdapter.Builder(this, items)
        .addProducer(MusicCategoryViewHolder())
        .build { it }

    private fun recyclerViewFolderMusic() = RecyclerViewAdapter.Builder(this, viewModel.foldersMusic)
        .addProducer(MusicFoldersViewHolder())
        .build { it }

    private fun recyclerViewArtistAdapter(items: StateFlow<List<RecyclerViewBaseDataModel>>) = RecyclerViewAdapter.Builder(this, items)
        .addProducer(MusicArtistViewHolder())
        .build { it }


    private fun searchActive(){
        val searchView = binding.search
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

    override fun onStop() {
        super.onStop()
        binding.rvAllMusic.layoutManager = null
    }

    fun categoryItemClicked(id: Int){
        when (id){
            0 -> {
                viewModel._categoryList.value = getCategoryLists("artist", viewModel.loadingMusic.value).toRecyclerViewItemOfList()
                recyclerViewCategoryAdapter(viewModel.categoryList)
            }
            1 -> {

            }
            2 -> {

            }
            3 -> {

            }
            4 -> {

            }
        }
    }

    fun getCategoryLists(category: String, playlist: List<Track>): List<String>{
        val categoryList: MutableList<String> = mutableListOf()
        playlist.forEach {
            if (it.artist == category && !categoryList.contains(it.artist)){
                categoryList.add(it.artist)
            }
        }
        return categoryList
    }

    private fun List<String>.toRecyclerViewItemOfList(): List<RecyclerViewBaseDataModel>{
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it,
            RV_ITEM_MUSIC_CATEGORY
        )) }
        return newList
    }


}



