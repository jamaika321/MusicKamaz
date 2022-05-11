package ru.kamaz.music.ui

import android.R
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.*
import ru.kamaz.music.databinding.FragmentListBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.ui.category.CategoryFragment
import ru.kamaz.music.ui.producers.MusicFoldersViewHolder
import ru.kamaz.music.ui.producers.MusicListViewHolderProducer
import ru.kamaz.music.view_models.list.ListViewModel
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.base.recycler_view.RecyclerViewAdapter
import java.io.File


class FolderFragment:BaseFragment<ListViewModel, FragmentListBinding>(ListViewModel::class.java) {
    override fun inject(app: BaseApplication) {
        app.getComponent<MusicComponent>().inject(this)
    }

    private val scopeIO = CoroutineScope(Dispatchers.IO + Job())

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )= FragmentListBinding.inflate(inflater, container, false)

    override fun setListeners() {
        super.setListeners()
    }

    private fun recyclerViewAdapter() = RecyclerViewAdapter.Builder(this, viewModel.items)
        .addProducer(MusicFoldersViewHolder())
        .build { it }

    override fun initVars() {
        binding.folderWithMusicRv.layoutManager = GridLayoutManager(context, 5)
        binding.folderWithMusicRv.adapter = recyclerViewAdapter()
    }






}