package ru.kamaz.music.domain

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.tw.john.TWUtil
import android.util.Log
import com.tw.auxin.AuxInActivity

//ByAirat and Chinese man

interface TestSettings {

    fun start(start: (Int) -> Unit)
    fun stop()

    class Base(private val settingsUtil: TWUtil): TestSettings {

        companion object{
            private const val TAG = "AuxInActivity"
            private const val MEssageDelay: Int = 0xff9999
        }

        private var MSGBYTE: Int = 0

        private var onHandleMessage: (Int) -> Unit = { }

        private var mTWEQUtilHandler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                Log.i("ReviewTest_AUX222", " ${msg.what}: ${msg.arg1} : ${msg.arg2}")
                when (msg.what) {
                    0x201 -> {
                        MSGBYTE = msg.arg2
                        this.removeMessages(MEssageDelay)
                        sendEmptyMessageDelayed(MEssageDelay, 150)
                    }
                    0x020c -> {

                    }
                    514 -> {
                        Log.i("ReviewTest_AUX22", "handleMessage: ")
                    }

                    MEssageDelay -> onHandleMessage.invoke(MSGBYTE)
                }
            }
        }

        override fun start(start: (Int) -> Unit) {
            onHandleMessage = start
//            settingsUtil.open(shortArrayOf(0x0301))
//            settingsUtil.start()
            onResume()
//            settingsUtil.write(0x0301)
        }

        private fun onResume(){
//            settingsUtil.write(0x020c)
            settingsUtil.addHandler(TAG, mTWEQUtilHandler)
            requestSource(true)
            requestBrake()
        }

        fun requestBrake() {
            settingsUtil.write(128, 1)
        }

        fun requestSource(lois: Boolean) {
            if (lois) {
                settingsUtil!!.write(REQUEST_SOURCE2, 1 shl 7 or (1 shl 6), 0x07)
            } else {
                settingsUtil!!.write(REQUEST_SOURCE, 1 shl 7 or (1 shl 6), 0x87)
            }
        }

        override fun stop() {
            requestSource(false)
            settingsUtil.removeHandler(TAG)
            settingsUtil.close()
        }
    }

    companion object {
        private const val TAG = "AuxInActivity"
        const val REQUEST_BRAKE = 0x0205
        const val RETURN_BRAKE = 0x0205
        const val REQUEST_SOURCE2 = 0x0301
        const val REQUEST_SOURCE = 0x9e11
        const val REQUEST_SERVICE = 0x9e00
        private const val GONE = 0xff00
        private const val CAMERA_HIDE = 0xff0e
        private const val SIGNAL = 0xff0f
        const val ACTIVITY_RUSEME = 0x07
        const val ACTIVITY_PAUSE = 0x87
    }
}