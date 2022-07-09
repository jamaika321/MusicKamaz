package ru.kamaz.music.ui.producers

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music.ui.producers.ItemType.RV_ITEM_MUSIC_PLAYLIST
import ru.kamaz.music.view_models.music_category.ItemPlayListViewModel
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.presentation.base.recycler_view.ViewHolderProducer


class MusicPlayListViewHolder : ViewHolderProducer<PlayListModel, ItemPlayListViewModel, MainCategoryItemBinding>(
    RV_ITEM_MUSIC_PLAYLIST , PlayListModel::class.java, ItemPlayListViewModel::class.java
) {
    override fun initBinding(inflater: LayoutInflater, parent: ViewGroup)=
        MainCategoryItemBinding.inflate(inflater, parent, false)
}