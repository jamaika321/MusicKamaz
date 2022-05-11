package ru.kamaz.widget.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import ru.kamaz.music_api.BaseConstants.ACTION_NEXT
import ru.kamaz.music_api.BaseConstants.ACTION_PREV
import ru.kamaz.music_api.BaseConstants.ACTION_TOGGLE_PAUSE
import ru.kamaz.widget.R
import ru.kamaz.widget.ui.base.BaseAppWidget


/**
 * Implementation of App Widget functionality.
 */
class MusicWidget : BaseAppWidget() {
    private var pendingIntent: PendingIntent? = null
    override fun performUpdate(context: Context, appWidgetIds: IntArray?) {
        val appWidgetView = RemoteViews(context.packageName, R.layout.music_widget)

        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (pendingIntent == null) {

        }
        manager.setRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime(),
            60000,
            pendingIntent
        )

        linkButtons(context, appWidgetView)
        pushUpdate(context, appWidgetIds, appWidgetView)
    }

    fun updateArtist(context: Context, artist: String) {
        val appWidgetView = RemoteViews(context.packageName, R.layout.music_widget)

        appWidgetView.setTextViewText(R.id.music_name, artist)

        pushUpdate(context, null, appWidgetView)
    }
    fun updateTitle(context: Context, title: String) {
        val appWidgetView = RemoteViews(context.packageName, R.layout.music_widget)

        appWidgetView.setTextViewText(R.id.artist_name, title)

        pushUpdate(context, null, appWidgetView)
    }

    private fun linkButtons(context: Context, views: RemoteViews) {
        var pendingIntent: PendingIntent
        val serviceName = ComponentName(context, "ru.kamaz.music.services.MusicService")

        pendingIntent = buildPendingIntent(context, ACTION_TOGGLE_PAUSE, serviceName)
        views.setOnClickPendingIntent(R.id.play_pause_widget, pendingIntent)

        pendingIntent = buildPendingIntent(context, ACTION_NEXT, serviceName)
        views.setOnClickPendingIntent(R.id.next_widget, pendingIntent)

        pendingIntent = buildPendingIntent(context, ACTION_PREV, serviceName)
        views.setOnClickPendingIntent(R.id.prev_widget, pendingIntent)

        Log.i("linkBTN", "linkBTN")
    }

    companion object {
        private var mInstance: MusicWidget? = null
        val appWidgetViewId = R.layout.music_widget

        val instance: MusicWidget
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = MusicWidget()
                }
                return mInstance!!
            }
    }
}