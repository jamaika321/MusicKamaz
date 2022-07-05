package ru.kamaz.music.view_models

import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.core.widget.PopupWindowCompat
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.R
import ru.kamaz.music.databinding.TestTextItemBinding
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music.ui.fragments.TrackFragment
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted
import java.io.File

class ItemViewModel: RecyclerViewBaseItem<Track, TestTextItemBinding>(){
    private val artist = MutableStateFlow("")
    private val title =MutableStateFlow("")
    private val image = MutableStateFlow("")
    private val playing = MutableStateFlow(false)
    private val favorite = MutableStateFlow(false)
    private val _position = MutableStateFlow(0)
    private lateinit var data: Track

    override fun bindData(data: Track, position: Int) {
        this.data = data
        artist.value= data.artist
        title.value= data.title
        image.value = data.albumArt
        playing.value = data.playing
        favorite.value = data.favorite
        _position.value = position
    }

    override fun initVars() {
        artist.launchWhenStarted(parent.lifecycleScope){
            binding.artistName.text=it
        }
        title.launchWhenStarted(parent.lifecycleScope){
            binding.musicName.text=it
        }
        image.launchWhenStarted(parent.lifecycleScope){
            if (it != "") {
                Picasso.with(parent.context)
                    .load(Uri.fromFile(File(image.value)))
                    .into(binding.image)
            } else {
                binding.image.setImageResource(R.drawable.music_png_bg)
            }
        }

        playing.launchWhenStarted(parent.lifecycleScope){
            if (it){
                binding.foregroundImage.visibility = View.VISIBLE
                binding.mainLayoutMusicItem.setBackgroundResource(R.drawable.back_item_true)
            }else{
                binding.foregroundImage.visibility = View.INVISIBLE
                binding.mainLayoutMusicItem.setBackgroundResource(R.drawable.ic_back_item)
            }
        }

        favorite.launchWhenStarted(parent.lifecycleScope){
            if (it){
                binding.like.setImageResource(R.drawable.ic_like_true)
            } else {
                binding.like.setImageResource(R.drawable.ic_like_false)
            }
        }

        binding.settings.setOnClickListener {
            (parent as MainListMusicFragment).onOptionsItemClicked(_position.value, data)
        }

        binding.root.setOnClickListener {
           (parent as MainListMusicFragment).onTrackClicked(data)
        }

        binding.like.setOnClickListener {
            (parent as MainListMusicFragment).onLikeClicked(data)
            favorite.value = !favorite.value
        }
    }
}