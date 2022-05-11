package ru.kamaz.music.services

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.util.Log
import com.eckom.xtlibrary.twproject.presenter.BasePresenter
import com.eckom.xtlibrary.twproject.service.BaseMusicService

class TestService: BaseMusicService(){

    val CMDPREV = "prev"
    val CMDNEXT = "next"
    val CMDPP = "pp"
    val CMDUPDATE = "update"
    val ACTIONCMD = "com.tw.music.action.cmd"
    val ACTIONPREV = "com.tw.music.action.prev"
    val ACTIONNEXT = "com.tw.music.action.next"
    val ACTIONPP = "com.tw.music.action.pp"

    class MusicBinder : Binder() {
        val service: MusicService
            get() = this.service
    }


    override fun onCreate() {
        super.onCreate()

        val commandFilter = IntentFilter()
        commandFilter.addAction(ACTIONCMD)
        commandFilter.addAction(ACTIONPREV)
        commandFilter.addAction(ACTIONNEXT)
        commandFilter.addAction(ACTIONPP)
        registerReceiver(mIntentReceiver, commandFilter)
    }

    private val mIntentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.i("onReceive", "onReceive: CMDUPDATE")
            val cmd = intent.getStringExtra("cmd")
            if (CMDPREV == cmd || ACTIONPREV == action) {
                Log.i("onReceive", "onReceive: CMDUPDATE")
            } else if (CMDNEXT == cmd ||ACTIONNEXT == action) {
                Log.i("onReceive", "onReceive: CMDUPDATE")
            } else if (CMDPP == cmd || ACTIONPP == action) {
                Log.i("onReceive", "onReceive: CMDUPDATE")
            } else if (CMDUPDATE == cmd) {
                Log.i("onReceive", "onReceive: CMDUPDATE")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            val cmd = intent.getStringExtra("cmd")
            if (CMDPREV == cmd || ACTIONPREV == action) {
                Log.i("onReceive", "onReceive: CMDUPDATE")
            } else if (CMDNEXT == cmd || ACTIONNEXT == action) {
                Log.i("onReceive", "onReceive: CMDUPDATE")
            } else if (CMDPP == cmd || ACTIONPP == action) {
                Log.i("onReceive", "onReceive: CMDUPDATE")
            }
        }
        return START_STICKY
    }

    override fun getPresenter(): BasePresenter<*, *> {
        TODO("Not yet implemented")
    }

    override fun onShuffleRepeat(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onHomeChick() {
        TODO("Not yet implemented")
    }


}