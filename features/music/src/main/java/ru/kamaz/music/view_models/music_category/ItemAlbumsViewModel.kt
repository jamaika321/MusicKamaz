package ru.kamaz.music.view_models.music_category

import android.net.Uri
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.databinding.AlbumItemsBinding
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted
import java.io.File


class ItemAlbumsViewModel: RecyclerViewBaseItem<Track, AlbumItemsBinding>(){
    private val artist = MutableStateFlow("")
    private val title = MutableStateFlow(0)
    private val image = MutableStateFlow("")
    private lateinit var data: Track


    override fun initVars() {
        artist.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text= it
        }
        title.launchWhenStarted(parent.lifecycleScope){

        }
        image.launchWhenStarted(parent.lifecycleScope){
            if (it.isNotEmpty()) {
                Picasso.with(parent.context)
                    .load(Uri.fromFile(File(it.trim())))
                    .into(binding.imageCategory)
            }
        }


        binding.root.setOnClickListener {
            (parent as MainListMusicFragment).openAlbumsPlayList(data.album)
        }
    }

    override fun bindData(data: Track, position: Int) {
        this.data = data
        artist.value= data.album
        image.value = data.albumArt
    }


}