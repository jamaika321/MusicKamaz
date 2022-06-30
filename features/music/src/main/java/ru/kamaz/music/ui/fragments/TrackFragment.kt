package ru.kamaz.music.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.StateFlow
import ru.kamaz.music.databinding.FragmentListMusicBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.ui.producers.MusicListViewHolderProducer
import ru.kamaz.music.view_models.TrackViewModel
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.base.recycler_view.RecyclerViewAdapter
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import ru.sir.presentation.extensions.launchOn
import ru.sir.presentation.extensions.launchWhenStarted

class TrackFragment() :
    BaseFragment<TrackViewModel, FragmentListMusicBinding>(TrackViewModel::class.java ) {

    override fun inject(app: BaseApplication) {
        app.getComponent<MusicComponent>().inject(this)
    }

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentListMusicBinding {
        return FragmentListMusicBinding.inflate(inflater, container, false)
    }

    override fun initVars(){
        viewModel.service.launchWhenStarted(lifecycleScope) {
            if (it == null) return@launchWhenStarted
            initServiceVars()
        }
    }


    private fun initServiceVars(){
        changeRVItems()
        viewModel.isUsbModeOn.launchOn(lifecycleScope){
            if (it){
                viewModel.changeSource("USB")
            } else {
                viewModel.changeSource("DISK")
            }
//            viewModel.loadDiskPlaylist("5")
        }

        setFragmentResultListener("lastMusic") { key, bundle ->
            val result = bundle.getString("bundleKey")
            if (!result.isNullOrEmpty())  viewModel.lastMusic.value = result
        }
        setFragmentResultListener("sourceEnum") { key, bundle ->
            when (bundle.getString("bundleKey")){
                "USB" -> viewModel.changeSource("USB")
                "DISK" -> viewModel.changeSource("DISK")
            }
        }


        binding.rvAllMusic.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 5){
                    binding.search.visibility = View.INVISIBLE
                }
                if (dy < -1){
                    binding.search.visibility = View.VISIBLE
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        viewModel.trackIsEmpty.launchOn(lifecycleScope) {
            musicListIsEmpty(it)
        }

        viewModel.lastMusicChanged.launchWhenStarted(lifecycleScope){
            viewModel.lastMusic(it)
        }
    }

    private fun changeRVItems(){
        binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.itemsAll)
    }

    override fun setListeners() {
        binding.search.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                binding.search.setIconified(false)
                searchActive()
            }
        })

        super.setListeners()
    }
    fun onTrackClicked(track: Track) {
        viewModel.onItemClick(track , track.data)
    }

    fun onLikeClicked(track: Track) {
        viewModel.onLikeClicked(track)
    }

    private fun recyclerViewAdapter(items : StateFlow<List<RecyclerViewBaseDataModel>>) = RecyclerViewAdapter.Builder(this, items)
        .addProducer(MusicListViewHolderProducer())
        .build { it }

    private fun  musicListIsEmpty(isEmpty:Boolean){
        if (isEmpty) binding.audioIsEmpty.visibility = View.VISIBLE
        else binding.audioIsEmpty.visibility = View.GONE
    }

    private fun searchActive(){
        val searchView = binding.search as SearchView

        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener
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

    override fun onStop() {
        binding.rvAllMusic.layoutManager = null
        super.onStop()
    }


}



