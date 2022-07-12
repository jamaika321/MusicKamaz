package ru.kamaz.music.view_models.recyclerViewModels

import android.net.Uri
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.MutableStateFlow
import ru.kamaz.music.R
import ru.kamaz.music.databinding.PlaylistItemBinding
import ru.kamaz.music.ui.fragmentDialog.DialogAddTrack
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted
import java.io.File

class AddTrackViewModel: RecyclerViewBaseItem<PlayListModel, PlaylistItemBinding>() {

    private val title = MutableStateFlow("")
    private val image = MutableStateFlow("")
    private val id = MutableStateFlow(0L)
    private val selection = MutableStateFlow(false)
    lateinit var data: PlayListModel

    override fun initVars() {
        selection.launchWhenStarted(parent.lifecycleScope){
            if (it) {
                binding.foregroundImage.visibility = View.VISIBLE
            } else {
                binding.foregroundImage.visibility = View.INVISIBLE
            }
        }
        title.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text = title.value
        }
        image.launchWhenStarted(parent.lifecycleScope){
            if (it.isNotEmpty()) {
                Picasso.with(parent.context)
                    .load(Uri.fromFile(File(image.value.trim())))
                    .into(binding.imageCategory)
            } else {
                binding.imageCategory.setImageResource(R.drawable.ic_play_list)
            }
        }
        binding.root.setOnClickListener {
            (parent as DialogAddTrack).selectPlayList(data.id, data.title)
        }
    }

    override fun bindData(data: PlayListModel, position: Int) {
        title.value = data.title
        image.value = data.albumArt
        id.value = data.id
        selection.value = data.selection
        this.data = data
    }


}