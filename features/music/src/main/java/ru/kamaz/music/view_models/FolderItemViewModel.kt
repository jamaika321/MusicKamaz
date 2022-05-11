package ru.kamaz.music.view_models

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music_api.models.AllFolderWithMusic
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted



class FolderItemViewModel: RecyclerViewBaseItem<AllFolderWithMusic, MainCategoryItemBinding>(){
    private val artist = MutableStateFlow("")
    private val title = MutableStateFlow(0)
    private lateinit var data: AllFolderWithMusic


    override fun initVars() {
        artist.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text= it.toString()
        }
        title.launchWhenStarted(parent.lifecycleScope){

        }

        binding.root.setOnClickListener {

        }
    }

    override fun bindData(data: AllFolderWithMusic) {
        this.data = data
        artist.value= data.dir
    }


}