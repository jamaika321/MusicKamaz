package ru.kamaz.music.ui.producers

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.kamaz.music.databinding.GenresItemBinding
import ru.kamaz.music.ui.producers.ItemType.RV_ITEM_MUSIC_GENRES
import ru.kamaz.music.view_models.GenresItemViewModel
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.ViewHolderProducer

class MusicGenresViewHolder : ViewHolderProducer<Track, GenresItemViewModel, GenresItemBinding>(
   RV_ITEM_MUSIC_GENRES, Track::class.java, GenresItemViewModel::class.java
) {
    override fun initBinding(inflater: LayoutInflater, parent: ViewGroup)= GenresItemBinding.inflate(inflater, parent, false)
}