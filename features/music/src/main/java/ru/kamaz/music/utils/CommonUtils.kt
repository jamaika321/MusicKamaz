package ru.kamaz.music.utils

import android.app.ActivityManager
import android.content.Context

class CommonUtils {
    companion object {
        fun isRunning(context: Context, serviceClass: Class<*>): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.className)) {
                    return true
                }
            }
            return false
        }
    }
}