package ru.kamaz.music.ui

import android.os.Bundle

inline fun <reified T> Bundle.getTypedSerializable(key: String): T? {
    val data = this.getSerializable(key)
    return if (data is T)
        data
    else
        null
}