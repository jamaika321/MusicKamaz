package ru.kamaz.music.ui.producers

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music.ui.producers.ItemType.RV_ITEM_MUSIC_CATEGORY
import ru.kamaz.music.view_models.recyclerViewModels.MusicCategoryViewModel
import ru.kamaz.music_api.models.CategoryMusicModel
import ru.sir.presentation.base.recycler_view.ViewHolderProducer

class MusicCategoryViewHolder: ViewHolderProducer<CategoryMusicModel, MusicCategoryViewModel, MainCategoryItemBinding >(
   RV_ITEM_MUSIC_CATEGORY, CategoryMusicModel::class.java, MusicCategoryViewModel::class.java
) {
   override fun initBinding(inflater: LayoutInflater, parent: ViewGroup)= MainCategoryItemBinding.inflate(inflater,parent,false)
}