package ru.kamaz.music.ui.fragmentDialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kamaz.music.R
import ru.kamaz.music.databinding.DialogAddPlaylistBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music_api.interactor.InsertPlayList
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.presentation.base.BaseApplication
import javax.inject.Inject

class DialogAddPlaylistFragment : DialogFragment() {

    private var _binding: DialogAddPlaylistBinding? = null
    private val binding get() = _binding!!

    fun inject(app: BaseApplication){
        app.getComponent<MusicComponent>().inject(this)
    }

    @Inject
    lateinit var insertPlayList: InsertPlayList

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddPlaylistBinding.inflate(layoutInflater, null, false)
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        inject(activity?.application as BaseApplication)
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.50).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.60).toInt()
        dialog!!.window?.setLayout(width, height)
        setListener()
        binding.addButtons.btnClose.text = getString(R.string.cancel_add)
        binding.addButtons.btnAddToPlaylist.text = getString(R.string.create)
    }

    private fun addPlayList(){
        val newPlayList = binding.etAddPlayList.text.toString()
        CoroutineScope(Dispatchers.IO).launch {
            insertPlayList.run(
                InsertPlayList.Params(
                    PlayListModel(13L, newPlayList, "",  arrayListOf(""))
                )
            )
        }
    }

    private fun setListener() {
        binding.addButtons.btnAddToPlaylist.setOnClickListener {
            if (binding.etAddPlayList.text.isNullOrEmpty()){
                Toast.makeText(context, "Введите название.", Toast.LENGTH_SHORT).show()
            } else {
                addPlayList()
                onDestroyView()
            }
        }
        binding.addButtons.btnClose.setOnClickListener {
            onDestroyView()
        }
    }


}