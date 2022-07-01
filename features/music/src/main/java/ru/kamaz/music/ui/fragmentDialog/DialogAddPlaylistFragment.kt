package ru.kamaz.music.ui.fragmentDialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import ru.kamaz.music.databinding.DialogAddPlaylistBinding
import ru.kamaz.music.view_models.DialogAddPlaylistFragmentViewModel

class DialogAddPlaylistFragment : DialogFragment() {

    private var _binding: DialogAddPlaylistBinding? = null
    var dialogAddPlaylistFragmentViewModelVM: DialogAddPlaylistFragmentViewModel? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddPlaylistBinding.inflate(layoutInflater, null, false)
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogAddPlaylistFragmentViewModelVM = ViewModelProvider(this).get(DialogAddPlaylistFragmentViewModel::class.java)
        //dialogAddPlaylistFragmentViewModelVM?.update()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.40).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, height)
        setListener()
    }

    fun addPlayList(){
        val newPlayList = binding.etAddPlayList.text.toString()
        if (newPlayList.isEmpty()) {
            Log.i("playList", "isEmpty$newPlayList")
        } else {
            Log.i("playList", "$newPlayList")
            dialogAddPlaylistFragmentViewModelVM?.update(newPlayList)
        }
    }

    fun setListener() {
        binding.btnGet.setOnClickListener {
            addPlayList()
            onDestroyView()
        }
        binding.btnClose.setOnClickListener {
            onDestroyView()
        }
    }


}