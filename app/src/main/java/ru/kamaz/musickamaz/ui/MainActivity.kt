package ru.kamaz.musickamaz.ui

import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import ru.kamaz.music.intentlauncher.managers.TileMusicManager
import ru.kamaz.music.intentlauncher.receivers.TileListenerReceiver
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
            NavAction.OPEN_ADD_PLAY_LIST_DIALOG -> {
                navigator.navigate(R.id.dialogAddPlaylistFragment)
            }
            NavAction.OPEN_MUSIC_FRAGMENT -> {
                navigator.navigate(R.id.musicFragment)
            }
            NavAction.OPEN_DIALOG_ADD_TRACK -> {
                navigator.navigate(R.id.addTrack)
            }
        }
    }

    override fun onActivityCreated() {
        checkPermission()
        TileMusicManager.sendMusicOn(this)
    }

    private fun checkPermission() {
        when (PermissionUtils.isPermissionsGranted(this)) {
            false -> requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, PermissionUtils.permissions, CODE_PERMISSION)
    }
}