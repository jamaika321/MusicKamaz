package ru.kamaz.music.ui.fragmentDialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import ru.kamaz.music.databinding.BtDialogFragmentBinding
import ru.kamaz.music.databinding.DialogAddTrackBinding

class DialogAddTrack : DialogFragment(){

    private var _binding: DialogAddTrackBinding? = null

    private val binding get() = _binding!!

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
        val width = (resources.displayMetrics.widthPixels * 0.70).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()
        dialog!!.window?.setLayout(width, height)
    }
}