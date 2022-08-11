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
//    private inner class CameraView {
//        private val mWindowManager: WindowManager
//        private val mLayoutParams: WindowManager.LayoutParams
//        private val mView: View
//        private var isShow = false
//        fun show() {
//            if (!isShow) {
//                val brightness = getIntPref("brightness", 0)
//                (mView.findViewById<View>(R.id.brightness_title) as TextView).text =
//                    getString(R.string.brightness) + "(" + brightness + ")"
//                (mView.findViewById<View>(R.id.brightness_level) as SeekBar).max = Companion.MAX
//                (mView.findViewById<View>(R.id.brightness_level) as SeekBar).progress =
//                    brightness + Companion.MAX / 2
//                val contrast = getIntPref("contrast", 0)
//                (mView.findViewById<View>(R.id.contrast_title) as TextView).text =
//                    getString(R.string.contrast) + "(" + contrast + ")"
//                (mView.findViewById<View>(R.id.contrast_level) as SeekBar).max = Companion.MAX
//                (mView.findViewById<View>(R.id.contrast_level) as SeekBar).progress =
//                    contrast + Companion.MAX / 2
//                val saturation = getIntPref("saturation", 0)
//                (mView.findViewById<View>(R.id.saturation_title) as TextView).text =
//                    getString(R.string.saturation) + "(" + saturation + ")"
//                (mView.findViewById<View>(R.id.saturation_level) as SeekBar).max = Companion.MAX
//                (mView.findViewById<View>(R.id.saturation_level) as SeekBar).progress =
//                    saturation + Companion.MAX / 2
//                isShow = true
//                mWindowManager.addView(mView, mLayoutParams)
//                mHandler.removeMessages(CAMERA_HIDE)
//                mHandler.sendEmptyMessageDelayed(CAMERA_HIDE, 5000)
//            }
//        }
//
//        fun hide() {
//            if (isShow) {
//                setIntPref(
//                    "brightness",
//                    (mView.findViewById<View>(R.id.brightness_level) as SeekBar).progress - Companion.MAX / 2
//                )
//                setIntPref(
//                    "contrast",
//                    (mView.findViewById<View>(R.id.contrast_level) as SeekBar).progress - Companion.MAX / 2
//                )
//                setIntPref(
//                    "saturation",
//                    (mView.findViewById<View>(R.id.saturation_level) as SeekBar).progress - Companion.MAX / 2
//                )
//                mWindowManager.removeView(mView)
//                isShow = false
//            }
//        }
//
//        companion object {
//            private const val MAX = 200
//        }
//
//        init {
//            mView = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
//                R.layout.camera,
//                null
//            )
//            val onSeekBarChangeListener: OnSeekBarChangeListener =
//                object : OnSeekBarChangeListener {
//                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
//                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
//                    override fun onProgressChanged(
//                        seekBar: SeekBar,
//                        progress: Int,
//                        fromUser: Boolean
//                    ) {
//                        var progress = progress
//                        if (fromUser) {
//                            progress -= Companion.MAX / 2
//                            when (seekBar.id) {
//                                R.id.brightness_level -> (mView.findViewById<View>(R.id.brightness_title) as TextView).text =
//                                    getString(R.string.brightness) + "(" + progress + ")"
//                                R.id.contrast_level -> (mView.findViewById<View>(R.id.contrast_title) as TextView).text =
//                                    getString(R.string.contrast) + "(" + progress + ")"
//                                R.id.saturation_level -> (mView.findViewById<View>(R.id.saturation_title) as TextView).text =
//                                    getString(R.string.saturation) + "(" + progress + ")"
//                            }
//                            var brightness =
//                                (mView.findViewById<View>(R.id.brightness_level) as SeekBar).progress - Companion.MAX / 2
//                            brightness += 32
//                            if (brightness < 0) {
//                                brightness = 0
//                            } else if (brightness > 255) {
//                                brightness = 255
//                            }
//                            var contrast =
//                                (mView.findViewById<View>(R.id.contrast_level) as SeekBar).progress - Companion.MAX / 2
//                            contrast += 102
//                            if (contrast < 0) {
//                                contrast = 0
//                            } else if (contrast > 255) {
//                                contrast = 255
//                            }
//                            var saturation =
//                                (mView.findViewById<View>(R.id.saturation_level) as SeekBar).progress - Companion.MAX / 2
//                            saturation += 100
//                            if (saturation < 0) {
//                                saturation = 0
//                            } else if (saturation > 255) {
//                                saturation = 255
//                            }
//                            if (mCamera != null) mCamera.setAnalogInputColor(
//                                brightness,
//                                contrast,
//                                saturation
//                            )
//                            mHandler.removeMessages(CAMERA_HIDE)
//                            mHandler.sendEmptyMessageDelayed(CAMERA_HIDE, 5000)
//                        }
//                    }
//                }
//            (mView.findViewById<View>(R.id.brightness_level) as SeekBar).setOnSeekBarChangeListener(
//                onSeekBarChangeListener
//            )
//            (mView.findViewById<View>(R.id.contrast_level) as SeekBar).setOnSeekBarChangeListener(
//                onSeekBarChangeListener
//            )
//            (mView.findViewById<View>(R.id.saturation_level) as SeekBar).setOnSeekBarChangeListener(
//                onSeekBarChangeListener
//            )
//            val onClickListener =
//                View.OnClickListener { view ->
//                    when (view.id) {
//                        R.id.brightness_down -> {
//                            var progress =
//                                (mView.findViewById<View>(R.id.brightness_level) as SeekBar).progress - Companion.MAX / 2
//                            if (--progress < -Companion.MAX / 2) {
//                                progress = -Companion.MAX / 2
//                            }
//                            (mView.findViewById<View>(R.id.brightness_title) as TextView).text =
//                                getString(R.string.brightness) + "(" + progress + ")"
//                            (mView.findViewById<View>(R.id.brightness_level) as SeekBar).progress =
//                                progress + Companion.MAX / 2
//                        }
//                        R.id.brightness_up -> {
//                            var progress =
//                                (mView.findViewById<View>(R.id.brightness_level) as SeekBar).progress - Companion.MAX / 2
//                            if (++progress > Companion.MAX / 2) {
//                                progress = Companion.MAX / 2
//                            }
//                            (mView.findViewById<View>(R.id.brightness_title) as TextView).text =
//                                getString(R.string.brightness) + "(" + progress + ")"
//                            (mView.findViewById<View>(R.id.brightness_level) as SeekBar).progress =
//                                progress + Companion.MAX / 2
//                        }
//                        R.id.contrast_down -> {
//                            var progress =
//                                (mView.findViewById<View>(R.id.contrast_level) as SeekBar).progress - Companion.MAX / 2
//                            if (--progress < -Companion.MAX / 2) {
//                                progress = -Companion.MAX / 2
//                            }
//                            (mView.findViewById<View>(R.id.contrast_title) as TextView).text =
//                                getString(R.string.contrast) + "(" + progress + ")"
//                            (mView.findViewById<View>(R.id.contrast_level) as SeekBar).progress =
//                                progress + Companion.MAX / 2
//                        }
//                        R.id.contrast_up -> {
//                            var progress =
//                                (mView.findViewById<View>(R.id.contrast_level) as SeekBar).progress - Companion.MAX / 2
//                            if (++progress > Companion.MAX / 2) {
//                                progress = Companion.MAX / 2
//                            }
//                            (mView.findViewById<View>(R.id.contrast_title) as TextView).text =
//                                getString(R.string.contrast) + "(" + progress + ")"
//                            (mView.findViewById<View>(R.id.contrast_level) as SeekBar).progress =
//                                progress + Companion.MAX / 2
//                        }
//                        R.id.saturation_down -> {
//                            var progress =
//                                (mView.findViewById<View>(R.id.saturation_level) as SeekBar).progress - Companion.MAX / 2
//                            if (--progress < -Companion.MAX / 2) {
//                                progress = -Companion.MAX / 2
//                            }
//                            (mView.findViewById<View>(R.id.saturation_title) as TextView).text =
//                                getString(R.string.saturation) + "(" + progress + ")"
//                            (mView.findViewById<View>(R.id.saturation_level) as SeekBar).progress =
//                                progress + Companion.MAX / 2
//                        }
//                        R.id.saturation_up -> {
//                            var progress =
//                                (mView.findViewById<View>(R.id.saturation_level) as SeekBar).progress - Companion.MAX / 2
//                            if (++progress > Companion.MAX / 2) {
//                                progress = Companion.MAX / 2
//                            }
//                            (mView.findViewById<View>(R.id.saturation_title) as TextView).text =
//                                getString(R.string.saturation) + "(" + progress + ")"
//                            (mView.findViewById<View>(R.id.saturation_level) as SeekBar).progress =
//                                progress + Companion.MAX / 2
//                        }
//                    }
//                    var brightness =
//                        (mView.findViewById<View>(R.id.brightness_level) as SeekBar).progress - Companion.MAX / 2
//                    brightness += 32
//                    if (brightness < 0) {
//                        brightness = 0
//                    } else if (brightness > 255) {
//                        brightness = 255
//                    }
//                    var contrast =
//                        (mView.findViewById<View>(R.id.contrast_level) as SeekBar).progress - Companion.MAX / 2
//                    contrast += 102
//                    if (contrast < 0) {
//                        contrast = 0
//                    } else if (contrast > 255) {
//                        contrast = 255
//                    }
//                    var saturation =
//                        (mView.findViewById<View>(R.id.saturation_level) as SeekBar).progress - Companion.MAX / 2
//                    saturation += 100
//                    if (saturation < 0) {
//                        saturation = 0
//                    } else if (saturation > 255) {
//                        saturation = 255
//                    }
//                    if (mCamera != null) mCamera.setAnalogInputColor(
//                        brightness,
//                        contrast,
//                        saturation
//                    )
//                    mHandler.removeMessages(CAMERA_HIDE)
//                    mHandler.sendEmptyMessageDelayed(CAMERA_HIDE, 5000)
//                }
//            mView.findViewById<View>(R.id.brightness_down).setOnClickListener(onClickListener)
//            mView.findViewById<View>(R.id.brightness_up).setOnClickListener(onClickListener)
//            mView.findViewById<View>(R.id.contrast_down).setOnClickListener(onClickListener)
//            mView.findViewById<View>(R.id.contrast_up).setOnClickListener(onClickListener)
//            mView.findViewById<View>(R.id.saturation_down).setOnClickListener(onClickListener)
//            mView.findViewById<View>(R.id.saturation_up).setOnClickListener(onClickListener)
//            mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
//            mLayoutParams = WindowManager.LayoutParams(
//                450, 260, 0, 0,
//                WindowManager.LayoutParams.TYPE_TOAST,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.RGBA_8888
//            )
//        }
//    }
//
//    private fun getIntPref(name: String, def: Int): Int {
//        val prefs = getSharedPreferences(packageName, MODE_PRIVATE)
//        return prefs.getInt(name, def)
//    }
//
//    private fun setIntPref(name: String, value: Int) {
//        val prefs = getSharedPreferences(packageName, MODE_PRIVATE)
//        val ed = prefs.edit()
//        ed.putInt(name, value)
//        ed.commit()
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
//        try {
//            mCamera = Camera.open(mCameraId)
//            var brightness = getIntPref("brightness", 0)
//            brightness += 32
//            if (brightness < 0) {
//                brightness = 0
//            } else if (brightness > 255) {
//                brightness = 255
//            }
//            var contrast = getIntPref("contrast", 0)
//            contrast += 102
//            if (contrast < 0) {
//                contrast = 0
//            } else if (contrast > 255) {
//                contrast = 255
//            }
//            var saturation = getIntPref("saturation", 0)
//            saturation += 100
//            if (saturation < 0) {
//                saturation = 0
//            } else if (saturation > 255) {
//                saturation = 255
//            }
//            mCamera.setAnalogInputColor(brightness, contrast, saturation)
//        } catch (e: Exception) {
//        }
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
//    fun onClick(v: View) {
//        when (v.id) {
//            R.id.home -> {
//                val it = Intent(Intent.ACTION_MAIN)
//                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                it.addCategory(Intent.CATEGORY_HOME)
//                startActivity(it)
//            }
//            R.id.back -> finish()
//        }
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