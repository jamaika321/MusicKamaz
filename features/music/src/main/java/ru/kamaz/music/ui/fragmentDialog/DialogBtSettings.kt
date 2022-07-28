package ru.kamaz.music.ui.fragmentDialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import ru.kamaz.music.R
import ru.kamaz.music.databinding.BtDialogFragmentBinding


class DialogBtSettings : DialogFragment() {

    private var _binding: BtDialogFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = BtDialogFragmentBinding.inflate(layoutInflater, null, false)
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
        val width = (resources.displayMetrics.widthPixels * 0.50).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, height)
        setListener()
    }

    fun setListener(){
        binding.clButtons.btnAddToPlaylist.text = getString(R.string.close)
        binding.clButtons.btnClose.text = getString(R.string.settings)
        binding.clButtons.btnAddToPlaylist.setOnClickListener {
            onDestroyView()
        }  
        binding.clButtons.btnClose.setOnClickListener {
            openBluetoothSettings()
        }
    }

    private fun openBluetoothSettings() {
        val intent = Intent(Intent.ACTION_MAIN, null)
        val componentName = ComponentName("ru.bis.settings", "ru.bis.settings.presentation.StartActivity")
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.component = componentName
        intent.putExtra("BluetoothSettings", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        startActivity(intent)
    }


}