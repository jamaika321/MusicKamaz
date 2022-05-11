package ru.kamaz.music.view_models.music_category

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted


class ItemPlayListViewModel : RecyclerViewBaseItem<PlayListModel, MainCategoryItemBinding>(){

    private val playlist = MutableStateFlow("")
    private lateinit var data: PlayListModel

    override fun initVars() {
        playlist.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text = it
        }

        binding.root.setOnClickListener {

        }
    }
    override fun bindData(data: PlayListModel) {
        this.data = data
        playlist.value = data.title
    }
}