package ru.kamaz.music.ui.fragmentDialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.kamaz.music.databinding.DialogAddTrackBinding
import ru.kamaz.music.ui.producers.AddTrackViewHolder
import ru.kamaz.music.ui.producers.ItemType
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.presentation.base.recycler_view.RecyclerViewAdapter
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel

class DialogAddTrack : DialogFragment(){

    private var _binding: DialogAddTrackBinding? = null

    private val binding get() = _binding!!

    private val playList = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddTrackBinding.inflate(layoutInflater, null, false)
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.80).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()
        dialog!!.window?.setLayout(width, height)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.rvAllMusic.layoutManager = GridLayoutManager(context, 4)
        binding.rvAllMusic.adapter = recyclerViewAdapter(playList)
        setListeners()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun setListeners(){
        binding.addButtons.cancelBtn.setOnClickListener {
            onDestroyView()
        }
        binding.addButtons.addBtn.setOnClickListener {
            openAddDialog()
        }
    }

    private fun openAddDialog(){

    }

    fun selectPlayList(id: Long, title: String){


    }

    private fun recyclerViewAdapter(
        items: StateFlow<List<RecyclerViewBaseDataModel>>
    ): RecyclerViewAdapter<List<RecyclerViewBaseDataModel>> {
        return RecyclerViewAdapter.Builder(this, items)
                    .addProducer(AddTrackViewHolder())
                    .build { it }

    }

    private fun List<PlayListModel>.toRecyclerViewItems(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, ItemType.RV_ITEM_MUSIC_PLAYLIST)) }
        return newList
    }

}