package ru.kamaz.music.ui.fragmentDialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.fragment.app.DialogFragment
import ru.kamaz.music.databinding.FragmentTrackOptionBinding
import ru.kamaz.music.ui.NavAction
import ru.sir.presentation.navigation.UiAction

class TrackOptionFragment : DialogFragment() {

    private var _binding : FragmentTrackOptionBinding? = null

    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentTrackOptionBinding.inflate(layoutInflater, null, false)
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
//        dialog!!.window?.attributes?.x = 222
//        dialog!!.window?.attributes?.y = 85
        val width = (resources.displayMetrics.widthPixels * 0.40).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, height)
        setListener()
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
    }

    private fun setListener(){
        binding.btnDelete.setOnClickListener {
            dialog?.dismiss()
        }
        binding.btnAddToPlaylist.setOnClickListener {
            dialog?.dismiss()
        }

    }


}