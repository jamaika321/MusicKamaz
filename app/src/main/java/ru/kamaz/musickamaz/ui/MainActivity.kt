package ru.kamaz.musickamaz.ui

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import ru.kamaz.music.ui.NavAction
import ru.kamaz.music.ui.PermissionUtils
import ru.kamaz.musickamaz.R
import ru.sir.presentation.base.BaseActivity
import ru.sir.presentation.navigation.UiAction

class MainActivity : BaseActivity() {
    private val CODE_PERMISSION = 1337

    override val layoutId: Int = R.layout.activity_main
    override fun getNavController() = findNavController(R.id.navHostFragment)

    override fun navigateTo(action: UiAction) {
        when (action.id) {
            NavAction.OPEN_TRACK_LIST_FRAGMENT -> {
                navigator.navigate(R.id.mainListMusicFragment)
            }
            NavAction.OPEN_BT_FRAGMENT -> {
                navigator.navigate(R.id.btFragment)
            }
            NavAction.OPEN_DIALOG_BT_FRAGMENT -> {
                navigator.navigate(R.id.fragment_dialog)
            }
            NavAction.OPEN_ADD_PLAY_LIST_DIALOG -> {
                navigator.navigate(R.id.dialogAddPlaylistFragment)
            }
        }
    }

    override fun onActivityCreated() {
        checkPermission()
    }


    private fun checkPermission() {
        when (PermissionUtils.isPermissionsGranted(this)) {
            false -> requestPermissions()
        }
    }

    override fun onBackPressed() {
        Log.i("back", "onBackPressed: back")
        super.onBackPressed()
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, PermissionUtils.permissions, CODE_PERMISSION)
    }
}