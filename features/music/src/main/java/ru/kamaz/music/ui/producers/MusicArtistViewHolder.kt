package ru.kamaz.music.ui.producers
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.kamaz.music.databinding.FolderItemRvBinding
import ru.kamaz.music.ui.producers.ItemType.RV_ITEM_MUSIC_ARTIST
import ru.kamaz.music.view_models.music_category.ItemArtist
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.ViewHolderProducer


class MusicArtistViewHolder : ViewHolderProducer<Track, ItemArtist, FolderItemRvBinding>(
    RV_ITEM_MUSIC_ARTIST, Track::class.java, ItemArtist::class.java
) {
    override fun initBinding(inflater: LayoutInflater, parent: ViewGroup) =FolderItemRvBinding.inflate(inflater, parent, false)
}