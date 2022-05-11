package ru.kamaz.music.view_models

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.databinding.GenresItemBinding

import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted


class GenresItemViewModel: RecyclerViewBaseItem<Track, GenresItemBinding>(){
    private val artist = MutableStateFlow("")
    private val title = MutableStateFlow(0)
    private lateinit var data: Track


    override fun initVars() {
        artist.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text= it.toString()
        }
        title.launchWhenStarted(parent.lifecycleScope){

        }

        binding.root.setOnClickListener {

        }
    }

    override fun bindData(data: Track) {
        this.data = data
        artist.value= data.album
    }


}