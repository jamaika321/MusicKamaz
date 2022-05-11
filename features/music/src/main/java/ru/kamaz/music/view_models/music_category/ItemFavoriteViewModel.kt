package ru.kamaz.music.view_models.music_category

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music_api.models.FavoriteSongs
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted

class ItemFavoriteViewModel : RecyclerViewBaseItem<FavoriteSongs, MainCategoryItemBinding>(){
    private val artist = MutableStateFlow("")
    private val title =MutableStateFlow("")
    private lateinit var data: FavoriteSongs

    override fun initVars() {
        title.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text = it
        }
        binding.root.setOnClickListener {

        }
    }

    override fun bindData(data: FavoriteSongs) {
        this.data = data
        title.value = data.title

    }
}