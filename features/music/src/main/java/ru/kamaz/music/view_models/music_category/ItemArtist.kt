package ru.kamaz.music.view_models.music_category

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.R
import ru.kamaz.music.databinding.FolderItemRvBinding
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted


class ItemArtist : RecyclerViewBaseItem<Track, FolderItemRvBinding>(){
    private val artist = MutableStateFlow("")
    private val image = MutableStateFlow(0)

    override fun initVars() {

        artist.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text = it
            if (it == ""){
                binding.textCategory.text = "Красота в деталях"
            }
        }

        binding.root.setOnClickListener {
            binding.imageCategory.setImageResource(R.drawable.music_png_bg)
        }
    }
    override fun bindData(data: Track, position: Int) {
        artist.value = data.artist

    }
}