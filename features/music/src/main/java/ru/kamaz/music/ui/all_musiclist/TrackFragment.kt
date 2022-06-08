package ru.kamaz.music.ui.all_musiclist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.toColor
import androidx.lifecycle.lifecycleScope
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
        val mBinding = FragmentListMusicBinding.inflate(inflater, container, false)
//
        return mBinding
    }

    override fun initVars(){
        viewModel.service.launchWhenStarted(lifecycleScope) {
            if (it == null) return@launchWhenStarted
            initServiceVars()
        }
    }

    private fun initServiceVars(){
        viewModel.isNotConnectedUsb.launchWhenStarted(lifecycleScope){
            if (it){
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.itemsAll)
            } else {
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.items)
            }
        }

        viewModel.trackIsEmpty.launchOn(lifecycleScope) {
            musicListIsEmpty(it)
        }
    }

    override fun setListeners() {

        binding.search.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                binding.search.setIconified(false)
                searchActived()
            }
        })

        super.setListeners()
    }
    fun onTrackClicked(track: Track) {
        viewModel.onItemClick(track , track.data)
    }

    private fun recyclerViewAdapter(items : StateFlow<List<RecyclerViewBaseDataModel>>) = RecyclerViewAdapter.Builder(this, items)
        .addProducer(MusicListViewHolderProducer())
        .build { it }

    private fun  musicListIsEmpty(isEmpty:Boolean){
        if (isEmpty) binding.audioIsEmpty.visibility = View.VISIBLE
        else binding.audioIsEmpty.visibility = View.GONE
    }

    private fun searchActived(){
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


}



