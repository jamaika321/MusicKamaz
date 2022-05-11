package ru.kamaz.music.ui.producers

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music.ui.producers.ItemType.RV_ITEM_MUSIC_PLAYLIST_ADD_NEW
import ru.kamaz.music.view_models.music_category.ItemPlayListAddNewViewModel
import ru.kamaz.music_api.models.AddNewPlayListModel
import ru.sir.presentation.base.recycler_view.ViewHolderProducer


class MusicPlayListAddNewHolder :
    ViewHolderProducer<AddNewPlayListModel, ItemPlayListAddNewViewModel, MainCategoryItemBinding>(
        RV_ITEM_MUSIC_PLAYLIST_ADD_NEW,
        AddNewPlayListModel::class.java,
        ItemPlayListAddNewViewModel::class.java
    ) {
    override fun initBinding(inflater: LayoutInflater, parent: ViewGroup) =
        MainCategoryItemBinding.inflate(inflater, parent, false)

}