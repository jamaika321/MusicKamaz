package ru.kamaz.music.ui.fragments

import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.StateFlow
import ru.kamaz.music.databinding.FragmentCategoryMusicBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.domain.GlobalConstants
import ru.kamaz.music.ui.NavAction
import ru.kamaz.music.ui.enums.PlayListFlow
import ru.kamaz.music.ui.getTypedSerializable
import ru.kamaz.music.ui.producers.*
import ru.kamaz.music.view_models.music_category.CategoryViewModel
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.base.recycler_view.RecyclerViewAdapter
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import ru.sir.presentation.extensions.launchWhenStarted
import ru.sir.presentation.navigation.UiAction

class CategoryFragment : BaseFragment<CategoryViewModel, FragmentCategoryMusicBinding>(CategoryViewModel::class.java) {
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
    ) = FragmentCategoryMusicBinding.inflate(inflater, container, false)

    override fun initVars() {
        binding.rvCategory.layoutManager = GridLayoutManager(context, 5)
//        binding.rvCategory.adapter = recyclerViewAdapter2()
        viewModel.closeBack.launchWhenStarted(lifecycleScope){
            if (it) super.onBackPressed()
            Log.i("resBack", "buttonListener")
        }
        Log.i("category2", "initVars: ")
    }

    fun clickListener(id: Int) {
        Log.i("category2", "initVars:$id ")
        when (id) {
            0 -> {
                clickAddNewPlayList()
            }
            1 -> {
                binding.rvCategory.layoutManager = GridLayoutManager(context, 4)
                binding.rvCategory.adapter = recyclerViewAdapterArtist()
            }
            2 -> {
                binding.rvCategory.adapter = recyclerViewAdapterGenres()
            }
            3 -> {
                Log.i("ReviewTest_binddata", " 3button pressed ")
                binding.rvCategory.adapter = recyclerViewAdapterAlbums()
            }
            4 -> {
                Log.i("ReviewTest_binddata", " 4button pressed ")
                binding.rvCategory.adapter = recyclerViewAdapterFavorite()

            }
            5->{
                Log.i("ReviewTest_binddata", " 5button pressed ")
                binding.rvCategory.adapter = recyclerViewAdapterPlayList()
            }

        }
    }

    override fun onBackPressed() {
        Log.i("resBack", "onBackPressed")
        viewModel.onClickBack()
    }


    fun clickAddNewPlayList() {
        dialog()
    }

    override fun onStop() {
        binding.rvCategory.layoutManager = null
        super.onStop()
    }

    override fun onDestroy() {
        Log.i("fragmentState", "onDestroy:CategoryFragment ")
        super.onDestroy()
    }

    private fun dialog(){

        navigator.navigateTo(
            UiAction(
                NavAction.OPEN_ADD_PLAY_LIST_DIALOG
            )
        )
    }

    private fun getFavoritePlayList(){

    }


    private fun recyclerViewAdapter2() = RecyclerViewAdapter.Builder(this, viewModel.huitems)
        .addProducer(MusicCategoryViewHolder())
        .build { it }

    private fun recyclerViewAdapterFavorite() =
        RecyclerViewAdapter.Builder(this, viewModel.favorite)
            .addProducer(MusicFavoriteViewHolder())
            .build { it }

    private fun recyclerViewAdapterArtist() = RecyclerViewAdapter.Builder(this, viewModel.artist)
        .addProducer(MusicArtistViewHolder())
        .build { it }

    private fun recyclerViewAdapterPlayList() =
        RecyclerViewAdapter.Builder(this, viewModel.playlist)
            .addProducer(MusicPlayListViewHolder())
            .build { it }

    private fun recyclerViewAdapterGenres() =
        RecyclerViewAdapter.Builder(this, viewModel.genres)
            .addProducer(MusicGenresViewHolder())
            .build { it }
    private fun recyclerViewAdapterAlbums() =
        RecyclerViewAdapter.Builder(this, viewModel.albums)
            .addProducer(MusicAlbumsViewHolder())
            .build { it }

}