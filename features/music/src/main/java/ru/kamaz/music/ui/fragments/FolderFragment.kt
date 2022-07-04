package ru.kamaz.music.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.*
import ru.kamaz.music.databinding.FragmentListBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.ui.producers.MusicFoldersViewHolder
import ru.kamaz.music.view_models.list.FolderViewModel
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.base.recycler_view.RecyclerViewAdapter


class FolderFragment:BaseFragment<FolderViewModel, FragmentListBinding>(FolderViewModel::class.java) {
    override fun inject(app: BaseApplication) {
        app.getComponent<MusicComponent>().inject(this)
    }

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )= FragmentListBinding.inflate(inflater, container, false)

    override fun onDestroy() {
        Log.i("fragmentState", "onDestroy:FolderFragment ")
        super.onDestroy()
    }

    private fun recyclerViewAdapter() = RecyclerViewAdapter.Builder(this, viewModel.items)
        .addProducer(MusicFoldersViewHolder())
        .build { it }

    override fun initVars() {
        binding.folderWithMusicRv.layoutManager = GridLayoutManager(context, 5)
        binding.folderWithMusicRv.adapter = recyclerViewAdapter()
    }







}