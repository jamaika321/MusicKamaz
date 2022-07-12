package ru.kamaz.music.view_models.recyclerViewModels

import android.net.Uri
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.databinding.GenresItemBinding

import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted
import java.io.File


class GenresItemViewModel: RecyclerViewBaseItem<Track, GenresItemBinding>(){
    private val genres = MutableStateFlow("")
    private val title = MutableStateFlow("")
    private val image = MutableStateFlow("")
    private lateinit var data: Track


    override fun initVars() {
        genres.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text = it
        }
        title.launchWhenStarted(parent.lifecycleScope){

        }
        image.launchWhenStarted(parent.lifecycleScope){

        }

        binding.root.setOnClickListener {

        }
    }

    override fun bindData(data: Track, position: Int) {
        this.data = data
        genres.value= data.genre
    }


}