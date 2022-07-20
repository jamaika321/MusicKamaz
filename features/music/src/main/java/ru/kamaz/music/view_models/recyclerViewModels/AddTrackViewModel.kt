package ru.kamaz.music.view_models.recyclerViewModels

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
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

class AddTrackViewModel: RecyclerViewBaseItem<PlayListModel, PlaylistItemBinding>() {

    private val title = MutableStateFlow("")
    private val image = MutableStateFlow("")
    private val id = MutableStateFlow(0L)
    lateinit var data: PlayListModel

    override fun bindData(data: PlayListModel, position: Int) {
        title.value = data.title
        image.value = data.albumArt
        id.value = data.id
        this.data = data
    }

    override fun initVars() {
        title.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text = title.value
        }
        image.launchWhenStarted(parent.lifecycleScope) {
            if (it == "create_playlist") {
                binding.imageCategory.setImageResource(R.drawable.ic_plus)
            } else if (it.isNotEmpty()) {
                Picasso.with(parent.context)
                    .load(Uri.fromFile(File(image.value.trim())))
                    .into(binding.imageCategory)
            } else {
                binding.imageCategory.setImageResource(R.drawable.ic_play_list)
            }
        }
        binding.clAllItem.setOnClickListener {
            if (data.albumArt == "create_playlist"){
                (parent as MainListMusicFragment).addNewPlaylist()
            }
        }
        binding.clAllItem.setOnLongClickListener {
            if (data.albumArt != "create_playlist") {
                val popupMenu = PopupMenu(binding.root.context, it)
                popupMenu.inflate(R.menu.context_menu_playlist)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.delete -> {
                            (parent as MainListMusicFragment).deletePlayList(this.data.title)
                            Toast.makeText(parent.context, "Delete", Toast.LENGTH_SHORT).show()
                        }
                        R.id.playing -> {
                            Toast.makeText(parent.context, "Playing", Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }

                popupMenu.show()
            }
            return@setOnLongClickListener true
        }
    }




}