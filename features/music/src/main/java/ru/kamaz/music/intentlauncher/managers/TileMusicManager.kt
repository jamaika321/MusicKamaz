package ru.kamaz.music.intentlauncher.managers

import android.content.ComponentName
import android.content.Context
import android.content.Intent

class TileMusicManager(private val context: Context) {

    companion object {
        private fun createIntent(action: String) = Intent().also {
            it.component = ComponentName(
                "ru.kamaz.launcher",
                "ru.kamaz.media.presentation.screens.music.receiver.MusicListenerReceiver"
            )
            it.action = action
        }

        fun sendMusicOn(context: Context) =
            context.sendBroadcast(createIntent("ACTION_MUSIC_ON"))
    }


    fun sendTitle(title: String) {
        context.sendBroadcast(createIntent("ACTION_TITLE_MUSIC").apply {
            putExtra("title", title)
        })
    }

    fun sendArtist(artist: String) {
        context.sendBroadcast(createIntent("ACTION_ARTIST_MUSIC").apply {
            putExtra("artist", artist)
        })
    }

    fun sendIsPlaying(isPlaying: Boolean) {
        context.sendBroadcast(createIntent("ACTION_IS_PLAYING").apply {
            putExtra("isPlaying", isPlaying)
        })
    }

    fun sendSourceMusicType(type: Int) {
        context.sendBroadcast(createIntent("ACTION_SOURCE_MUSIC").apply {
            putExtra("source", type)
        })
    }

    fun sendAlbumImagePath(path: String) {
        context.sendBroadcast(createIntent("ACTION_IMAGE_PATH_MUSIC").apply {
            putExtra("image", path)
        })
    }
}