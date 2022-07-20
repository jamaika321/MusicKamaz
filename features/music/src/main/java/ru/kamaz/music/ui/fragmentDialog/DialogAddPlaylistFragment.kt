package ru.kamaz.music.ui.fragmentDialog

import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kamaz.music.databinding.DialogAddPlaylistBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.domain.TestSettings
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music.view_models.DialogAddPlaylistFragmentViewModel
import ru.kamaz.music_api.interactor.InsertPlayList
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.presentation.base.BaseApplication
import javax.inject.Inject

class DialogAddPlaylistFragment : DialogFragment() {

    private var _binding: DialogAddPlaylistBinding? = null
    var dialogAddPlaylistFragmentViewModelVM: DialogAddPlaylistFragmentViewModel? = null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogAddPlaylistFragmentViewModelVM = ViewModelProvider(this).get(DialogAddPlaylistFragmentViewModel::class.java)
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

    private fun addPlayList(){
        val newPlayList = binding.etAddPlayList.text.toString()
        CoroutineScope(Dispatchers.IO).launch {
            insertPlayList.run(
                InsertPlayList.Params(
                    PlayListModel(13L, newPlayList, "", listOf(""), listOf(""))
                )
            )
        }
        dialogAddPlaylistFragmentViewModelVM?.savePlayListOnDB(newPlayList)
    }

    private fun setListener() {
        binding.btnGet.setOnClickListener {
            if (binding.etAddPlayList.text.isNullOrEmpty()){
                Toast.makeText(context, "Введите название.", Toast.LENGTH_SHORT).show()
            } else {
                addPlayList()
                onDestroyView()
            }
        }
        binding.btnClose.setOnClickListener {
            onDestroyView()
        }
    }


}