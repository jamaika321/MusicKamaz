package ru.kamaz.music.ui.all_musiclist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
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

        setFragmentResultListener("sourceEnum") { key, bundle ->
            val result = bundle.getInt("bundleKey")
            when (result){
                2 -> viewModel.changeSource("2")
                3 -> viewModel.changeSource("3")
            }
        }
        return FragmentListMusicBinding.inflate(inflater, container, false)
    }

    override fun initVars(){
        when (viewModel.sourceEnum.value){
            true -> binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.items)
            false -> binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.itemsUsb)
        }

    }

    override fun setListeners() {
        viewModel.trackIsEmpty.launchOn(lifecycleScope){
            musicListIsEmpty(it)
        }
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

}

