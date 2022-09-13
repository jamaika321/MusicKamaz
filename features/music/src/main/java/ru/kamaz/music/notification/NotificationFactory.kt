package ru.kamaz.music.notification

import android.app.Notification
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.kamaz.music.R
import javax.inject.Inject


class NotificationFactory @Inject constructor(val context: Context) {

    private fun createNotification(model: SkeletalNotification): Notification {
        return NotificationCompat.Builder(context, model.channelId)
            .setSmallIcon(R.drawable.ic_play_list)
            .setContentTitle(model.title)
            .setContentText(model.content)
            .setPriority(model.priority)
            .setAutoCancel(true)
            .apply {
                when (model) {
                    is TextExpandableNotification -> {
                        setStyle(NotificationCompat.BigTextStyle().bigText(model.longText))
                    }
                    is PictureExpandableNotification -> {
                        val pictureBitmap =
                            BitmapFactory.decodeResource(context.resources, model.picture)
                        setLargeIcon(pictureBitmap)
                        setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(pictureBitmap)
                                .bigLargeIcon(null)
                                .setBigContentTitle(model.bigContentTitle)
                        )
                    }
                    is InboxNotification -> {
                        val inboxStyle = NotificationCompat.InboxStyle()

                        model.lines.forEach {
                            inboxStyle.addLine(it)
                        }

                        setStyle(inboxStyle)
                    }
                }

                model.actions.forEach { (iconId, title, actionIntent) ->
                    addAction(iconId, title, actionIntent)
                }
            }
            .build()
    }

    fun showNotification(id: Int, notification: SkeletalNotification) {
        NotificationManagerCompat
            .from(context)
            .notify(id, createNotification(notification))
    }
}