package ru.kamaz.music.view_models.music_category

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.R
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music.ui.fragments.CategoryFragment
import ru.kamaz.music_api.models.AddNewPlayListModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted

class ItemPlayListAddNewViewModel: RecyclerViewBaseItem<AddNewPlayListModel, MainCategoryItemBinding>(){
    private val addPlayList = MutableStateFlow("")
    private lateinit var data: AddNewPlayListModel

    override fun initVars() {
        addPlayList.launchWhenStarted(parent.lifecycleScope){
            binding.imageCategory.setImageResource(R.drawable.ic_plus)
        }

//        binding.root.setOnClickListener {
//            data?.let { (parent as CategoryFragment).dialog() }
//        }
    }

    override fun bindData(data: AddNewPlayListModel, position: Int) {
        this.data = data
        addPlayList.value = data.text
    }
}