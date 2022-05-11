package ru.kamaz.widget.ui.base

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.RemoteViews

abstract class BaseAppWidget : AppWidgetProvider(){

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        performUpdate(context, appWidgetIds)
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

    /**
     * Check against [AppWidgetManager] if there are any instances of this widget.
     */
    private fun hasInstances(context: Context): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val mAppWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(
                context, javaClass
            )
        )
        return mAppWidgetIds.isNotEmpty()
    }

    protected fun buildPendingIntent(
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

    abstract fun performUpdate(context: Context, appWidgetIds: IntArray?)

    companion object {

        const val NAME: String = "app_widget"

        fun createRoundedBitmap(
            drawable: Drawable?,
            width: Int,
            height: Int,
            tl: Float,
            tr: Float,
            bl: Float,
            br: Float
        ): Bitmap? {
            if (drawable == null) {
                return null
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val c = Canvas(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(c)

            val rounded = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(rounded)
            val paint = Paint()
            paint.shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            paint.isAntiAlias = true
            canvas.drawPath(
                composeRoundedRectPath(
                    RectF(0f, 0f, width.toFloat(), height.toFloat()), tl, tr, bl, br
                ), paint
            )

            return rounded
        }

        fun createBitmap(drawable: Drawable, sizeMultiplier: Float): Bitmap {
            val bitmap = Bitmap.createBitmap(
                (drawable.intrinsicWidth * sizeMultiplier).toInt(),
                (drawable.intrinsicHeight * sizeMultiplier).toInt(),
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(bitmap)
            drawable.setBounds(0, 0, c.width, c.height)
            drawable.draw(c)
            return bitmap
        }

        protected fun composeRoundedRectPath(
            rect: RectF,
            tl: Float,
            tr: Float,
            bl: Float,
            br: Float
        ): Path {
            val path = Path()
            path.moveTo(rect.left + tl, rect.top)
            path.lineTo(rect.right - tr, rect.top)
            path.quadTo(rect.right, rect.top, rect.right, rect.top + tr)
            path.lineTo(rect.right, rect.bottom - br)
            path.quadTo(rect.right, rect.bottom, rect.right - br, rect.bottom)
            path.lineTo(rect.left + bl, rect.bottom)
            path.quadTo(rect.left, rect.bottom, rect.left, rect.bottom - bl)
            path.lineTo(rect.left, rect.top + tl)
            path.quadTo(rect.left, rect.top, rect.left + tl, rect.top)
            path.close()

            return path
        }
    }
}