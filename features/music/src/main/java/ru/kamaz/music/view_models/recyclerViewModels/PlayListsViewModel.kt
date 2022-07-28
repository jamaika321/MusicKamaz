package ru.kamaz.music.view_models.recyclerViewModels

import android.net.Uri
import android.view.View
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

    override fun initVars() {
        title.launchWhenStarted(parent.lifecycleScope) {
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
        selection.launchWhenStarted(parent.lifecycleScope){
            if (it){
                binding.foregroundImage.visibility = View.VISIBLE
            } else {
                binding.foregroundImage.visibility = View.INVISIBLE
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
            (parent as MainListMusicFragment).playListItemLongClickListener(data.title)
            return@setOnLongClickListener true
        }
    }




}