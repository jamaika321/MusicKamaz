package ru.kamaz.music.cache

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SharedPrefsManager @Inject constructor(
    private val prefs: SharedPreferences,
    private val context: Context
) {
    companion object {
        private const val LAST_MUSIC = "last_music"
    }
    fun getLastMusic(): String = prefs.getString(LAST_MUSIC, "") ?: ""
    fun saveMusicInfo(musicId:String){prefs.edit().putString(LAST_MUSIC,musicId).apply()
    }
}