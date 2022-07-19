package ru.kamaz.music.view_models.recyclerViewModels

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music_api.models.CategoryMusicModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted

class MusicCategoryViewModel :RecyclerViewBaseItem<CategoryMusicModel, MainCategoryItemBinding>() {
    private val img = MutableStateFlow(0)
    private val category = MutableStateFlow("")
    private lateinit var data: CategoryMusicModel

    override fun initVars() {
        img.launchWhenStarted(parent.lifecycleScope){
            binding.imageCategory.setImageResource(it)
        }
        category.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text = it
        }
        binding.clAllItem.setOnClickListener {
            data.let {item ->
                (parent as MainListMusicFragment).categoryItemClicked(item.id)
            }
        }


    }

    override fun bindData(data: CategoryMusicModel, position: Int) {
        this.data = data
        img.value= data.img
        category.value= data.category

    }
}