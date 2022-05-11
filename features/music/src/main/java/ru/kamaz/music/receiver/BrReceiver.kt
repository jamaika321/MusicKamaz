package ru.kamaz.music.receiver

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_INSERT
import android.util.Log
import android.widget.Toast


private const val TAG = "BrReceiver"

class BrReceiver : BroadcastReceiver() {

    var CMDPREV = "prev"
    var CMDNEXT = "next"
    var CMDPP = "pp"
    var CMDUPDATE = "update"
    var ACTIONCMD = "com.tw.music.action.cmd"
    var ACTIONPREV = "com.tw.music.action.prev"
    var ACTIONNEXT = "com.tw.music.action.next"
    var ACTIONPP = "com.tw.music.action.pp"

    val WHERE_MY_CAT_ACTION = "ru.kamaz.musickamaz"
    override fun onReceive(context: Context, intent: Intent) {

      /*  Log.i(TAG, "onReceive: ${intent.action}")

        Log.i("recever", "onReceive: recever")
        val action = intent.action
        val cmd = intent.getStringExtra("cmd")
        val ppp = intent.getStringExtra(ACTION_INSERT)*/

        if (CMDPREV.equals(intent.action)) {
            Log.i("recever", "onReceive: recevernextTrack")
            toString().also { log ->
                Log.d(TAG, log)
                Toast.makeText(context, log, Toast.LENGTH_LONG).show()
            }
        } else if (CMDNEXT.equals(intent.action)) {
            Log.i("recever", "onReceive: recevernextTrack")
            toString().also { log ->
                Log.d(TAG, log)
                Toast.makeText(context, log, Toast.LENGTH_LONG).show()
            }
        } else if (CMDPP.equals(intent.action)) {
            Log.i("recever", "onReceive: receverplayOrPause")
            toString().also { log ->
                Log.d(TAG, log)
                Toast.makeText(context, log, Toast.LENGTH_LONG).show()
            }
        } else if (CMDUPDATE.equals(intent.action)) {
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            //mWidget.performUpdate(this@MusicService, appWidgetIds)
        }
        else if (ACTIONPREV.equals(intent.action)) {
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            Log.i("ACTIONPREV", "onReceive:ACTIONPREV")
        } else if (WHERE_MY_CAT_ACTION.equals(intent.action)) {
            Log.i("int", "onReceive:int")
        }

        else if (ACTIONCMD.equals(intent.action)) {
            Log.i("int", "onReceive:ACTIONCMD")
        }
        else if (ACTIONNEXT.equals(intent.action)) {
            Log.i("int", "onReceive:ACTIONNEXT")
        }
        else if (ACTIONPP.equals(intent.action)) {
            Log.i("int", "onReceive:ACTIONPP")
        }

    }
}