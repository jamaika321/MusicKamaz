//package com.tw.auxin
//
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.content.Intent
//import android.graphics.PixelFormat
//import android.hardware.Camera
//import android.os.Bundle
//import android.os.Handler
//import android.os.Message
//import android.tw.john.TWUtil
//import android.view.*
//import android.widget.ImageView
//import android.widget.RelativeLayout
//import android.widget.SeekBar
//import android.widget.SeekBar.OnSeekBarChangeListener
//import android.widget.TextView
//
//class AuxInActivity : Activity(), SurfaceHolder.Callback {
//    private var mTWUtil: TWUtil? = null
//    private var mCamera: Camera? = null
//    private val mCameraId = 7
//    private val mSurfaceView: SurfaceView? = null
//    private val mWarningImage: ImageView? = null
//    private var mCount = 0
//    private var mBrake = false
//    fun requestBrake() {
//        mTWUtil!!.write(REQUEST_BRAKE, 0xff)
//    }
//
//    private var w = 1024
//    private var h = 600
//    private var mPreview = false
//    fun setPreview(on: Boolean) {
//        if (on && !mPreview) {
//            mPreview = true
//            if (mCamera != null) {
//                mSurfaceView!!.visibility = View.VISIBLE
//                mCamera!!.startPreview()
//            }
//        } else if (!on && mPreview) {
//            if (mCamera != null) {
//                mCamera!!.stopPreview()
//                mSurfaceView!!.visibility = View.GONE
//            }
//            mPreview = false
//        }
//    }
//
//    private fun startPreview() {
//        if (mBrake) {
//            mWarningImage!!.visibility = View.GONE
//            mCount = 0
//            mHandler.removeMessages(SIGNAL)
//            mHandler.sendEmptyMessageDelayed(SIGNAL, 300)
//        } else {
//            mHandler.removeMessages(SIGNAL)
//            setPreview(false)
//            mWarningImage!!.setImageResource(R.drawable.warning_driving)
//            mWarningImage.visibility = View.VISIBLE
//        }
//    }
//
//    fun requestSource(`is`: Boolean) {
//        if (`is`) {
//            mTWUtil!!.write(REQUEST_SOURCE2, 1 shl 7 or (1 shl 6), 0x07)
//        } else {
//            mTWUtil!!.write(REQUEST_SOURCE, 1 shl 7 or (1 shl 6), 0x87)
//        }
//    }
//
//    private var mService = 0
//    fun requestService(activity: Int) {
//        mService = activity
//        mTWUtil!!.write(REQUEST_SERVICE, activity)
//    }
//
//    fun touch(x: Int, y: Int) {
//        mTWUtil!!.write(0x0802, x * 255 / (w - 1), y * 255 / (h - 1))
//    }
//
//    private var mCameraView: CameraView? = null
//    private var mHB: RelativeLayout? = null
//    private val mHandler: Handler = @SuppressLint("HandlerLeak")
//    object : Handler() {
//        override fun handleMessage(msg: Message) {
//            when (msg.what) {
//                CAMERA_HIDE -> mCameraView!!.hide()
//                SIGNAL -> {
//                    removeMessages(SIGNAL)
//                    mCount++
//                    if (mTWUtil!!.write(0x9f1d, mCameraId - 4) != 0) {
//                        setPreview(true)
//                        mWarningImage!!.visibility = View.GONE
//                    } else {
//                        setPreview(false)
//                        if (mCount > 10) {
//                            mWarningImage!!.setImageResource(R.drawable.warning_novideosignal)
//                            mWarningImage.visibility = View.VISIBLE
//                        }
//                    }
//                    sendEmptyMessageDelayed(SIGNAL, 100)
//                }
//                GONE -> mHB!!.visibility = View.GONE
//                RETURN_BRAKE -> {
//                    mBrake = msg.arg1 != 0
//                    startPreview()
//                }
//            }
//        }
//    }
//
//    public override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.auxin)
//        val holder = mSurfaceView!!.holder
//        holder.addCallback(this)
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
//        mHB = findViewById<View>(R.id.hb) as RelativeLayout
//        mCameraView = CameraView()
//        mTWUtil = TWUtil()
//        if (mTWUtil!!.open(shortArrayOf(0x0205)) != 0) {
//            finish()
//        }
//        mTWUtil!!.start()
//    }
//
//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_MENU) {
//            mCameraView!!.show()
//            return false
//        }
//        return super.onKeyDown(keyCode, event)
//    }
//
//    override fun onDestroy() {
//        requestSource(false)
//        mTWUtil!!.stop()
//        mTWUtil!!.close()
//        mTWUtil = null
//        super.onDestroy()
//    }
//
//    override fun onPause() {
//        mTWUtil!!.removeHandler(TAG)
//        mHandler.removeMessages(SIGNAL)
//        setPreview(false)
//        mWarningImage!!.visibility = View.GONE
//        if (mCamera != null) {
//            mCamera!!.release()
//            mCamera = null
//        }
//        requestService(ACTIVITY_PAUSE)
//        super.onPause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        requestService(ACTIVITY_RUSEME)
//        mTWUtil!!.addHandler(TAG, mHandler)
//        requestSource(true)
//        requestBrake()
//        startPreview()
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        when (event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                touch(event.x.toInt(), event.y.toInt())
//                if (mHB!!.visibility != View.VISIBLE) {
//                    mHB!!.visibility = View.VISIBLE
//                    mHandler.removeMessages(GONE)
//                    mHandler.sendEmptyMessageDelayed(GONE, 2000)
//                }
//            }
//            MotionEvent.ACTION_UP -> {}
//            MotionEvent.ACTION_MOVE -> {}
//            MotionEvent.ACTION_CANCEL -> {}
//        }
//        return super.onTouchEvent(event)
//    }
//
//    override fun surfaceChanged(holder: SurfaceHolder, format: Int, _w: Int, _h: Int) {
//        w = _w
//        h = _h
//    }
//
//    override fun surfaceCreated(holder: SurfaceHolder) {
//        try {
//            if (mCamera != null) {
//                mCamera!!.setPreviewDisplay(holder)
//            }
//        } catch (e: Exception) {
//        }
//    }
//
//    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        if (mCamera != null) {
//            mCamera!!.stopPreview()
//        }
//    }
//
//    companion object {
//        private const val TAG = "AuxInActivity"
//        const val REQUEST_BRAKE = 0x0205
//        const val RETURN_BRAKE = 0x0205
//        const val REQUEST_SOURCE2 = 0x0301
//        const val REQUEST_SOURCE = 0x9e11
//        const val REQUEST_SERVICE = 0x9e00
//        private const val GONE = 0xff00
//        private const val CAMERA_HIDE = 0xff0e
//        private const val SIGNAL = 0xff0f
//        const val ACTIVITY_RUSEME = 0x07
//        const val ACTIVITY_PAUSE = 0x87
//    }
//}