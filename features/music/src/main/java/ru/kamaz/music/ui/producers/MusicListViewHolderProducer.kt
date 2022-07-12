package ru.kamaz.music.ui.producers

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.kamaz.music.databinding.TestTextItemBinding
import ru.kamaz.music.ui.producers.ItemType.RV_ITEM
import ru.kamaz.music.view_models.recyclerViewModels.ItemViewModel
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.ViewHolderProducer

class MusicListViewHolderProducer: ViewHolderProducer<Track, ItemViewModel, TestTextItemBinding>(RV_ITEM, Track::class.java, ItemViewModel::class.java){
    override fun initBinding(inflater: LayoutInflater, parent: ViewGroup) =
        TestTextItemBinding.inflate(inflater,parent,false)

}