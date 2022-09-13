package ru.kamaz.music.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import ru.kamaz.music.R
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class NotificationChannelFactory @Inject constructor() {

    private val notificationChannels: MutableList<NotificationChannel> = arrayListOf()


    fun createChannel(channelInfo: NotificationChannelInfo): NotificationChannel =
        NotificationChannel(channelInfo.id, channelInfo.name, channelInfo.priority)
            .apply {
                this.description = channelInfo.description
            }

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val generalChannel = createChannel(
                NotificationChannelInfo(
                    "1",
                    "ThisIsMusica",
                    "AlarmManager",
                    NotificationManager.IMPORTANCE_MAX
                )
            )

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(generalChannel)
            notificationChannels.add(generalChannel)
        }
    }

    data class NotificationChannelInfo(
        val id: String,


        val name: String,
        val description: String,
        val priority: Int
    )
}