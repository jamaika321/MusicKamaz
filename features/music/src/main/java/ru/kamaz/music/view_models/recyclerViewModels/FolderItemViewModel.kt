package ru.kamaz.music.view_models.recyclerViewModels

import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.R
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music_api.models.AllFolderWithMusic
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted


class FolderItemViewModel: RecyclerViewBaseItem<AllFolderWithMusic, MainCategoryItemBinding>(){
    private val artist = MutableStateFlow("")
    private val title = MutableStateFlow(0)
    private lateinit var data: AllFolderWithMusic


    override fun initVars() {
        artist.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text= it
        }
        title.launchWhenStarted(parent.lifecycleScope){

        }

        binding.imageCategory.setImageResource(R.drawable.source_folder)




        binding.root.setOnClickListener {
            (parent as MainListMusicFragment).onFolderClicked(data.data, data.dir)
        }
    }

    override fun bindData(data: AllFolderWithMusic, position: Int) {
        this.data = data
        artist.value= data.dir
    }


}