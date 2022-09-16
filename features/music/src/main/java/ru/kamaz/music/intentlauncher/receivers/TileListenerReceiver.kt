package ru.kamaz.music.intentlauncher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TileListenerReceiver : BroadcastReceiver() {

    companion object {
        var prevMusic: () -> Unit = {}
        var nextMusic: () -> Unit = {}
        var playPauseMusic: () -> Unit = {}

        private const val ACTION_PREV_MUSIC = "ACTION_PREV_MUSIC"
        private const val ACTION_NEXT_MUSIC = "ACTION_NEXT_MUSIC"
        private const val ACTION_PLAY_PAUSE_MUSIC = "ACTION_PLAY_PAUSE_MUSIC"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            ACTION_PREV_MUSIC -> prevMusic.invoke()
            ACTION_NEXT_MUSIC -> nextMusic.invoke()
            ACTION_PLAY_PAUSE_MUSIC -> playPauseMusic.invoke()
        }
    }
}