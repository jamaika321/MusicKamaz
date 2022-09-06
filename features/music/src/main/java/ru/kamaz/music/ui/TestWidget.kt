package ru.kamaz.music.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.kamaz.music.R
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music_api.BaseConstants
import ru.sir.presentation.extensions.easyLog
import java.util.*

class TestWidget : AppWidgetProvider() {
    private var pendingIntent: PendingIntent? = null
    private var _isPlay = MutableStateFlow(true)
    val isPlay = _isPlay.asStateFlow()


    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()


    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        appWidgetIds?.forEach {
            updateAppWidget(context, appWidgetManager, appWidgetIds)
        }
    }

    protected fun pushUpdate(
        context: Context,
        appWidgetIds: IntArray?,
        views: RemoteViews
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (appWidgetIds != null) {
            appWidgetManager.updateAppWidget(appWidgetIds, views)
        } else {
            appWidgetManager.updateAppWidget(ComponentName(context, javaClass), views)
        }
    }

    private fun updateAppWidget(context: Context?,
                                 appWidgetManager: AppWidgetManager?,
                                 appWidgetIds: IntArray?) {
        val views = RemoteViews(context?.packageName, R.layout.test_widget)
        views.setTextViewText(R.id.widget_artist, "Shatal Ilshatov")

    }

    fun updateTestArtist(context: Context, artist: String) {
        val appWidgetView = RemoteViews(context.packageName, R.layout.test_widget)

        appWidgetView.setTextViewText(R.id.widget_title, artist)

        pushUpdate(context, null, appWidgetView)
    }

    fun updateTestTitle(context: Context, title: String) {
        val appWidgetView = RemoteViews(context.packageName, R.layout.test_widget)



        appWidgetView.setTextViewText(R.id.widget_artist, title)

        pushUpdate(context, null, appWidgetView)
    }

    fun updatePlayPauseImg(context: Context,plPause:Boolean){
        val appWidgetView = RemoteViews(context.packageName, R.layout.test_widget)
        if (plPause){
            "ic_play".easyLog(this)
            appWidgetView.setImageViewResource(R.id.play_pause_widget,R.drawable.ic_play_on_circle)
        }else{
            "ic_pause".easyLog(this)
            appWidgetView.setImageViewResource(R.id.play_pause_widget,R.drawable.ic_ic_circle_play_1)
        }
        pushUpdate(context, null, appWidgetView)
    }

    private fun linkButtons(context: Context, views: RemoteViews) {
        var pendingIntent: PendingIntent
        val serviceName = ComponentName(context, "ru.kamaz.music.services.MusicService")

        pendingIntent = buildPendingIntent(context, BaseConstants.ACTION_TOGGLE_PAUSE, serviceName)
        views.setOnClickPendingIntent(ru.kamaz.widget.R.id.play_pause_widget, pendingIntent)

        pendingIntent = buildPendingIntent(context, BaseConstants.ACTION_NEXT, serviceName)
        views.setOnClickPendingIntent(ru.kamaz.widget.R.id.next_widget, pendingIntent)

        pendingIntent = buildPendingIntent(context, BaseConstants.ACTION_PREV, serviceName)
        views.setOnClickPendingIntent(ru.kamaz.widget.R.id.prev_widget, pendingIntent)

        Log.i("linkBTN", "linkBTN")
    }

    private fun buildPendingIntent(
        context: Context,
        action: String,
        serviceName: ComponentName
    ): PendingIntent {
        val intent = Intent(action)
        intent.component = serviceName
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, 0, intent, 0)
        } else {
            PendingIntent.getService(context, 0, intent, 0)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }
    companion object {
        private var mInstance: TestWidget? = null
        val appWidgetViewId = R.layout.test_widget

        val instance: TestWidget
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = TestWidget()
                }
                return mInstance!!
            }
    }
}