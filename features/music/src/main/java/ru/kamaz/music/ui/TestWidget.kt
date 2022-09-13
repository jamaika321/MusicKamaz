package ru.kamaz.music.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.kamaz.music.R
import ru.kamaz.music.domain.TestSettings
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music_api.BaseConstants
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.BaseAppWidget
import ru.sir.presentation.extensions.easyLog
import java.io.File
import java.util.*

class TestWidget : BaseAppWidget() {
    private var pendingIntent: PendingIntent? = null
    private var _isPlay = MutableStateFlow(true)
    val isPlay = _isPlay.asStateFlow()


    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()


    override fun performUpdate(context: Context, appWidgetIds: IntArray?) {
        val appWidgetView = RemoteViews(context.packageName, R.layout.test_widget)

        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, MusicService::class.java)



        if (pendingIntent == null) {
            pendingIntent =
                PendingIntent.getService(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT)
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

    fun updateWidgetInfo(context: Context, artist: String, title: String){
        val appWidgetView = RemoteViews(context.packageName, R.layout.test_widget)

        appWidgetView.setTextViewText(R.id.widget_artist_name, artist)
        appWidgetView.setTextViewText(R.id.widget_music_name, title)

        pushUpdate(context, null, appWidgetView)
    }

    fun updateTestArtist(context: Context, artist: String) {
        val appWidgetView = RemoteViews(context.packageName, R.layout.test_widget)

        appWidgetView.setTextViewText(R.id.widget_artist_name, artist)

        pushUpdate(context, null, appWidgetView)
    }

    fun updateTestTitle(context: Context, title: String) {
        val appWidgetView = RemoteViews(context.packageName, R.layout.test_widget)

        appWidgetView.setTextViewText(R.id.widget_music_name, title)

        pushUpdate(context, null, appWidgetView)
    }

    fun updateTestImage(context: Context, cover: String) {
//        val appWidgetView = RemoteViews(context.packageName, R.layout.test_widget)
//
//        val image = Picasso.get()
//            .load(Uri.fromFile(File(cover.trim())))
//
//        CoroutineScope(Dispatchers.IO).launch {
//            appWidgetView.setImageViewBitmap(R.id.ll_back)
//        }
//        pushUpdate(context, null, appWidgetView)
    }

//    fun updateTestDuration(context: Context, title: Int) {
//        val appWidgetView = RemoteViews(context.packageName, R.layout.test_widget)
//
//        appWidgetView.setTextViewText(ru.kamaz.widget.R.id.duration_widget, Track.convertDuration(title.toLong()))
//
//        pushUpdate(context, null, appWidgetView)
//    }


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

    override fun onReceive(context: Context, intent: Intent?) {

        if (intent?.action == "WidgetClicked"){
            val appWidgetId = intent.getIntExtra("appWidgetId", 0)
            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
        }

        super.onReceive(context, intent)
    }
    companion object {
        private var mInstance: TestWidget? = null
        val appWidgetViewId = R.layout.test_widget

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, TestWidget::class.java)
            intent.action = "WidgetClicked"
            intent.putExtra("appWidgetId", appWidgetId)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val views = RemoteViews(context.packageName, R.layout.test_widget)
            views.setTextViewText(R.id.widget_music_name, "RERERERE")
            views.setOnClickPendingIntent(R.id.play_pause_widget, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        val instance: TestWidget
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = TestWidget()
                }
                return mInstance!!
            }
    }
}