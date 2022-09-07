package ru.kamaz.music.view_models.music_category

import android.net.Uri
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.databinding.FavoriteItemBinding
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music_api.models.FavoriteSongs
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted
import java.io.File

class ItemFavoriteViewModel : RecyclerViewBaseItem<Track, FavoriteItemBinding>(){
    private val artist = MutableStateFlow("")
    private val title = MutableStateFlow("")
    private val image = MutableStateFlow("")
    private lateinit var data: Track

    override fun initVars() {
        title.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text = it
        }
        image.launchWhenStarted(parent.lifecycleScope){
            if (it.isNotEmpty()) {
                Picasso.get()
                    .load(Uri.fromFile(File(it.trim())))
                    .into(binding.imageCategory)
            }
        }

        binding.root.setOnClickListener {

        }
    }

    override fun bindData(data: Track, position: Int) {
        this.data = data
        title.value = data.title
        artist.value = data.artist
        image.value = data.albumArt

    }
}