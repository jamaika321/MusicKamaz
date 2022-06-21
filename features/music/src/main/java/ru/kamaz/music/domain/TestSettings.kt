package ru.kamaz.music.domain

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import ru.kamaz.music.utils.TWSetting

interface TestSettings {

    fun start(start: (Int) -> Unit)
    fun stop()

    class Base(private val settingsUtil: TWSetting): TestSettings {

        companion object{
            private const val TAG = "TestSettings"
            private const val MEssageDelay: Int = 0xff9999
        }

        private var MSGBYTE: Int = 0

        private var onHandleMessage: (Int) -> Unit = { }

        private var mTWEQUtilHandler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    0x201 -> {
                        MSGBYTE = msg.arg2
                        this.removeMessages(MEssageDelay)
                        sendEmptyMessageDelayed(MEssageDelay, 150)
                    }
                    MEssageDelay -> onHandleMessage.invoke(MSGBYTE)
                }
            }
        }

        override fun start(start: (Int) -> Unit) {
            onHandleMessage = start
            settingsUtil.addHandler(TAG, mTWEQUtilHandler)
            settingsUtil.write(0x201, 0xff)
        }

        override fun stop() {
            settingsUtil.removeHandler(TAG)
            settingsUtil.close()
        }
    }
}