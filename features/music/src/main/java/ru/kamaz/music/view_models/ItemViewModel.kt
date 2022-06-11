package ru.kamaz.music.view_models


import android.net.Uri
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.databinding.TestTextItemBinding
import ru.kamaz.music.ui.all_musiclist.TrackFragment
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted
import java.io.File

class ItemViewModel: RecyclerViewBaseItem<Track, TestTextItemBinding>(){
    private val artist = MutableStateFlow("")
    private val title =MutableStateFlow("")
    private val image = MutableStateFlow("")
    private lateinit var data: Track

    override fun bindData(data: Track) {
        this.data = data
        artist.value= data.artist
        title.value= data.title
        image.value = data.albumArt
    }

    override fun initVars() {
        artist.launchWhenStarted(parent.lifecycleScope){
            binding.artistName.text=it
        }
        title.launchWhenStarted(parent.lifecycleScope){
            binding.musicName.text=it
        }
        image.launchWhenStarted(parent.lifecycleScope){
            Picasso.with(parent.context)
                .load(Uri.fromFile(File(image.value)))
                .into(binding.image)
        }

        binding.root.setOnClickListener {
           (parent as TrackFragment).onTrackClicked(data)
            Log.i("onTrackClicked", "onTrackClickedItemViewModel ")
        }
    }


}