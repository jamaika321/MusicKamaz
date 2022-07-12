package ru.kamaz.music.ui.producers

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.kamaz.music.databinding.PlaylistItemBinding
import ru.kamaz.music.view_models.recyclerViewModels.AddTrackViewModel
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.presentation.base.recycler_view.ViewHolderProducer

class AddTrackViewHolder: ViewHolderProducer<PlayListModel, AddTrackViewModel, PlaylistItemBinding>(
    ItemType.RV_ITEM_MUSIC_PLAYLIST, PlayListModel::class.java, AddTrackViewModel::class.java
) {
    override fun initBinding(inflater: LayoutInflater, parent: ViewGroup)= PlaylistItemBinding.inflate(inflater,parent,false)
}