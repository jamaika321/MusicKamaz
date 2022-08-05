package ru.kamaz.music.view_models.recyclerViewModels

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.R
import ru.kamaz.music.databinding.PlaylistItemBinding
import ru.kamaz.music.ui.fragmentDialog.DialogAddTrack
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted
import java.io.File
import java.lang.Exception
import kotlin.coroutines.coroutineContext

class PlayListsViewModel : RecyclerViewBaseItem<PlayListModel, PlaylistItemBinding>() {

    private val title = MutableStateFlow("")
    private val image = MutableStateFlow("")
    private val id = MutableStateFlow(0L)
    private val selection = MutableStateFlow(false)
    lateinit var data: PlayListModel

    override fun bindData(data: PlayListModel, position: Int) {
        title.value = data.title
        image.value = data.albumArt
        id.value = data.id
        selection.value = data.selection
        this.data = data
    }

    @SuppressLint("ResourceAsColor")
    override fun initVars() {
        title.launchWhenStarted(parent.lifecycleScope) {
            if (it != "create_050820221536"){
                binding.textCategory.text = title.value
            } else {
                binding.textCategory.text = parent.context?.getString(R.string.create)
            }
        }
        image.launchWhenStarted(parent.lifecycleScope) {
            if (it == "create_playlist") {
                binding.imageCategory.setImageResource(R.drawable.add_track_plus)
                binding.imageCategory.setPadding(60, 60, 60, 60)
            } else if (it.isNotEmpty()) {
                Picasso.with(parent.context)
                    .load(Uri.fromFile(File(image.value.trim())))
                    .into(binding.imageCategory)
            } else {
                binding.imageCategory.setImageResource(R.drawable.ic_play_list)
            }
        }
        selection.launchWhenStarted(parent.lifecycleScope){
            if (it){
                binding.foregroundImage.visibility = View.VISIBLE
                binding.textCategory.setTextColor(Color.parseColor("#437DFF"))
            } else {
                binding.foregroundImage.visibility = View.INVISIBLE
                binding.textCategory.setTextColor(Color.WHITE)
            }
        }
        setListeners()
    }

    private fun setListeners(){
        binding.clAllItem.setOnClickListener {
            if (data.albumArt == "create_playlist") {
                try {
                    (parent as MainListMusicFragment).addNewPlaylist()
                } catch (e: Exception) {
                    (parent as DialogAddTrack).addNewPlaylist()
                }
            } else {
                try {
                    (parent as DialogAddTrack).selectPlayList(id.value, title.value)
                } catch (e: Exception) {
                    (parent as MainListMusicFragment).playListSelected(title.value)
                }
            }
        }
        binding.clAllItem.setOnLongClickListener {
            if (data.albumArt != "create_playlist") {
                try {
                    (parent as MainListMusicFragment).playListItemLongClickListener(data.title)
                } catch (e: Exception) {
                    Log.e("Exception", "$e")
                }
            }
            return@setOnLongClickListener true
        }
    }




}