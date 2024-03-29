package ru.kamaz.music.ui.producers

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.kamaz.music.databinding.AlbumItemsBinding
import ru.kamaz.music.ui.producers.ItemType.RV_ITEM_MUSIC_ALBUMS
import ru.kamaz.music.view_models.music_category.ItemAlbumsViewModel
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.ViewHolderProducer


class MusicAlbumsViewHolder : ViewHolderProducer<Track, ItemAlbumsViewModel, AlbumItemsBinding>(
    RV_ITEM_MUSIC_ALBUMS, Track::class.java, ItemAlbumsViewModel::class.java
) {
    override fun initBinding(inflater: LayoutInflater, parent: ViewGroup)= AlbumItemsBinding.inflate(inflater, parent, false)
}