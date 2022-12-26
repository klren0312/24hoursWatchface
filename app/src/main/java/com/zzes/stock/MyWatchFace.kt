package com.zzes.stock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin


/**
 * Updates rate in milliseconds for interactive mode. We update once a second to advance the
 * second hand.
 */
private const val INTERACTIVE_UPDATE_RATE_MS = 1000

/**
 * Handler message id for updating the time periodically in interactive mode.
 */
private const val MSG_UPDATE_TIME = 0

private const val HOUR_STROKE_WIDTH = 5f
private const val MINUTE_STROKE_WIDTH = 3f
private const val SECOND_TICK_STROKE_WIDTH = 2f

private const val CENTER_GAP_AND_CIRCLE_RADIUS = 4f

private const val SHADOW_RADIUS = 6f

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn"t
 * shown. On devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient
 * mode. The watch face is drawn with less contrast in mute mode.
 *
 *
 * Important Note : Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
class MyWatchFace : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine {
        return Engine()
    }

    private class EngineHandler(reference: MyWatchFace.Engine) : Handler() {
        private val mWeakReference: WeakReference<MyWatchFace.Engine> = WeakReference(reference)

        override fun handleMessage(msg: Message) {
            val engine = mWeakReference.get()
            if (engine != null) {
                when (msg.what) {
                    MSG_UPDATE_TIME -> engine.handleUpdateTimeMessage()
                }
            }
        }
    }

    inner class Engine : CanvasWatchFaceService.Engine() {
        private lateinit var mSensorManager: SensorManager
        private lateinit var listener: SensorEventListener
        private var sensorRotate: Float = 0F

        private lateinit var mCalendar: Calendar

        private var cTimeArr = arrayListOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
        private var mRegisteredTimeZoneReceiver = false
        private var mMuteMode: Boolean = false
        private var mCenterX: Float = 0F
        private var mCenterY: Float = 0F

        private var mSecondHandLength: Float = 0F
        private var sMinuteHandLength: Float = 0F
        private var sHourHandLength: Float = 0F

        /* Colors for all hands (hour, minute, seconds, ticks) based on photo loaded. */
        private var mWatchHandColor: Int = 0
        private var mWatchHandHighlightColor: Int = 0
        private var mWatchHandShadowColor: Int = 0

        private lateinit var mHourPaint: Paint
        private lateinit var mMinutePaint: Paint
        private lateinit var mSecondPaint: Paint
        private lateinit var mTickHourTextPaint: Paint
        private lateinit var mTickLunarTextPaint: Paint
        private lateinit var mTickGuaTextPaint: Paint
        private lateinit var mDirectionPaint: Paint
        private lateinit var mBackgroundPaint: Paint

        private var mAmbient: Boolean = false
        private var mLowBitAmbient: Boolean = false
        private var mBurnInProtection: Boolean = false

        /* Handler to update the time once a second in interactive mode. */
        private val mUpdateTimeHandler = EngineHandler(this)

        private val mTimeZoneReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@MyWatchFace)
                    .setAcceptsTapEvents(true)
                    .build()
            )

            mCalendar = Calendar.getInstance()
            initServices()
            initializeBackground()
            initializeWatchFace()
        }

        private fun initServices() {
            // sensor manager
            mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            val accelerometerSensor: Sensor =
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magneticSensor: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            listener = object : SensorEventListener {
                var accelerometerValues = FloatArray(3)
                var magneticValues = FloatArray(3)
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                override fun onSensorChanged(event: SensorEvent) {
                    // 判断当前是加速度传感器还是地磁传感器
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        // 注意赋值时要调用clone()方法
                        accelerometerValues = event.values.clone()
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        // 注意赋值时要调用clone()方法
                        magneticValues = event.values.clone()
                    }
                    val rData = FloatArray(9)
                    val values = FloatArray(3)
                    SensorManager.getRotationMatrix(rData, null, accelerometerValues, magneticValues)
                    SensorManager.getOrientation(rData, values)
                    val degrees = Math.toDegrees(values[0].toDouble()).toFloat()
                    sensorRotate = (degrees + 360f) % 360f
//                    Log.d("MainActivity", "value[0] is " + Math.toDegrees(values[0].toDouble()))
                }
            }
            mSensorManager.registerListener(
                listener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            mSensorManager.registerListener(
                listener,
                magneticSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        private fun initializeBackground() {
            mBackgroundPaint = Paint().apply {
                color = Color.BLACK
            }
        }

        private fun initializeWatchFace() {
            /* Set defaults for colors */
            mWatchHandColor = Color.WHITE
            mWatchHandHighlightColor = Color.RED
            mWatchHandShadowColor = Color.BLACK

            mHourPaint = Paint().apply {
                color = mWatchHandColor
                strokeWidth = HOUR_STROKE_WIDTH
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
                setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
            }

            mMinutePaint = Paint().apply {
                color = mWatchHandColor
                strokeWidth = MINUTE_STROKE_WIDTH
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
                setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
            }

            mSecondPaint = Paint().apply {
                color = mWatchHandHighlightColor
                strokeWidth = SECOND_TICK_STROKE_WIDTH
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
                setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
            }

            mTickHourTextPaint = Paint().apply {
                color = mWatchHandColor
                strokeWidth = SECOND_TICK_STROKE_WIDTH
                isAntiAlias = true
                style = Paint.Style.STROKE
                setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
            }

            mDirectionPaint = Paint().apply {
                color = mWatchHandColor
                strokeWidth = SECOND_TICK_STROKE_WIDTH
                isAntiAlias = true
                style = Paint.Style.STROKE
                setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
            }

            mTickLunarTextPaint = Paint().apply {
                color = mWatchHandColor
                strokeWidth = SECOND_TICK_STROKE_WIDTH
                isAntiAlias = true
                style = Paint.Style.STROKE
                setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
            }

            mTickGuaTextPaint = Paint().apply {
                color = mWatchHandColor
                strokeWidth = SECOND_TICK_STROKE_WIDTH
                isAntiAlias = true
                style = Paint.Style.STROKE
                setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
            }
        }

        override fun onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            super.onDestroy()
            mSensorManager.unregisterListener(listener)
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)
            mLowBitAmbient = properties.getBoolean(
                WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false
            )
            mBurnInProtection = properties.getBoolean(
                WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false
            )
        }

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            mAmbient = inAmbientMode

            updateWatchHandStyle()

            // Check and trigger whether or not timer should be running (only
            // in active mode).
            updateTimer()
        }

        private fun updateWatchHandStyle() {
            if (mAmbient) {
                mHourPaint.color = Color.WHITE
                mMinutePaint.color = Color.WHITE
                mSecondPaint.color = Color.WHITE
                mTickHourTextPaint.color = Color.WHITE
                mDirectionPaint.color = Color.WHITE
                mTickLunarTextPaint.color = Color.WHITE
                mTickGuaTextPaint.color = Color.WHITE

                mHourPaint.isAntiAlias = false
                mMinutePaint.isAntiAlias = false
                mSecondPaint.isAntiAlias = false
                mTickHourTextPaint.isAntiAlias = false
                mDirectionPaint.isAntiAlias = false
                mTickLunarTextPaint.isAntiAlias = false
                mTickGuaTextPaint.isAntiAlias = false

                mHourPaint.clearShadowLayer()
                mMinutePaint.clearShadowLayer()
                mSecondPaint.clearShadowLayer()
                mTickHourTextPaint.clearShadowLayer()
                mDirectionPaint.clearShadowLayer()
                mTickLunarTextPaint.clearShadowLayer()
                mTickGuaTextPaint.clearShadowLayer()

            } else {
                mHourPaint.color = mWatchHandColor
                mMinutePaint.color = mWatchHandColor
                mSecondPaint.color = mWatchHandHighlightColor
                mTickHourTextPaint.color = mWatchHandColor
                mDirectionPaint.color = mWatchHandColor
                mTickLunarTextPaint.color = mWatchHandColor
                mTickGuaTextPaint.color = mWatchHandColor

                mHourPaint.isAntiAlias = true
                mMinutePaint.isAntiAlias = true
                mSecondPaint.isAntiAlias = true
                mTickHourTextPaint.isAntiAlias = true
                mDirectionPaint.isAntiAlias = true
                mTickLunarTextPaint.isAntiAlias = true
                mTickGuaTextPaint.isAntiAlias = true

                mHourPaint.setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
                mMinutePaint.setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
                mSecondPaint.setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
                mTickHourTextPaint.setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
                mTickHourTextPaint.setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
                mTickLunarTextPaint.setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
                mTickGuaTextPaint.setShadowLayer(
                    SHADOW_RADIUS, 0f, 0f, mWatchHandShadowColor
                )
            }
        }

        override fun onInterruptionFilterChanged(interruptionFilter: Int) {
            super.onInterruptionFilterChanged(interruptionFilter)
            val inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode
                mHourPaint.alpha = if (inMuteMode) 100 else 255
                mMinutePaint.alpha = if (inMuteMode) 100 else 255
                mSecondPaint.alpha = if (inMuteMode) 80 else 255
                invalidate()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mCenterX = width / 2f
            mCenterY = height / 2f

            /*
             * Calculate lengths of different hands based on watch screen size.
             */
            mSecondHandLength = (mCenterX * 0.875).toFloat()
            sMinuteHandLength = (mCenterX * 0.75).toFloat()
            sHourHandLength = (mCenterX * 0.5).toFloat()

        }

        /**
         * Captures tap event (and tap type). The [WatchFaceService.TAP_TYPE_TAP] case can be
         * used for implementing specific logic to handle the gesture.
         */
        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            when (tapType) {
                WatchFaceService.TAP_TYPE_TOUCH -> {
                    // The user has started touching the screen.
                }
                WatchFaceService.TAP_TYPE_TOUCH_CANCEL -> {
                    // The user has started a different gesture or otherwise cancelled the tap.
                }
                WatchFaceService.TAP_TYPE_TAP ->
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    Toast.makeText(applicationContext, R.string.message, Toast.LENGTH_SHORT)
                        .show()
            }
            invalidate()
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            val now = System.currentTimeMillis()
            mCalendar.timeInMillis = now

            drawBackground(canvas)
            drawGua(canvas)
            drawWatchFace(canvas)
        }

        private fun drawBackground(canvas: Canvas) {

            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.BLACK)
            } else if (mAmbient) {
                canvas.drawColor(Color.BLACK)
            } else {
                canvas.drawColor(Color.BLACK)
            }
        }

        private fun drawGua(canvas: Canvas) {
            var gua = arrayListOf<String>("坤", "乾", "兑", "离", "震", "巽", "坎", "艮")
            var guaToNum = mapOf("乾" to "111","兑" to "011","离" to "101","震" to "001","巽" to "110","坎" to "010","艮" to "100","坤" to "000")
            var gua64 = mapOf("111111" to "乾为天","111011" to "天泽履","111101" to "天火同人","111001" to "天雷无妄","111110" to "天风姤","111010" to "天水讼","111100" to "天山遁","111000" to "天地否","011111" to "泽天夬","011011" to "兑为泽","011101" to "泽火革","011001" to "泽雷随","011110" to "泽风大过","011010" to "泽水困","011100" to "泽山咸","011000" to "泽地萃","101111" to "火天大有","101011" to "火泽睽","101101" to "离为火","101001" to "火雷噬嗑","101110" to "火风鼎","101010" to "火水未济","101100" to "火山旅","101000" to "火地晋","001111" to "雷天大壮","001011" to "雷泽归妹","001101" to "雷火丰","001001" to "震为雷","001110" to "雷风恒","001010" to "雷水解","001100" to "雷山小过","001000" to "雷地豫","110111" to "风天小畜","110011" to "风泽中孚","110101" to "风火家人","110001" to "风雷益","110110" to "巽为风","110010" to "风水涣","110100" to "风山渐","110000" to "风地观","010111" to "水天需","010011" to "水泽节","010101" to "水火既济","010001" to "水雷屯","010110" to "水风井","010010" to "坎为水","010100" to "水山蹇","010000" to "水地比","100111" to "山天大畜","100011" to "山泽损","100101" to "山火贲","100001" to "山雷颐","100110" to "山风蛊","100010" to "山水蒙","100100" to "艮为山","100000" to "山地剥","000111" to "地天泰","000011" to "地泽临","000101" to "地火明夷","000001" to "地雷复","000110" to "地风升","000010" to "地水师","000100" to "地山谦","000000" to "坤为地")
            var cd: Calendar = Calendar.getInstance()
            var lunarDateUtil: LunarDateUtil = LunarDateUtil
            var lunarObj = lunarDateUtil.solar2lunar(cd.get(Calendar.YEAR), cd.get(Calendar.MONTH) + 1, cd.get(Calendar.DATE))
            var lYearIndex = lunarObj?.lYear
            var lMonthIndex = lunarObj?.lMonth
            var lDayIndex = lunarObj?.lDay
            var timeIndex = getTimeIndex()

            val shangIndex = (lYearIndex!! + lMonthIndex!! + lDayIndex!!) % 8
            val xiaIndex = (lYearIndex!! + lMonthIndex!! + lDayIndex!! + timeIndex) % 8
            val fullGuaNum = guaToNum[gua[shangIndex]] + guaToNum[gua[xiaIndex]]
            var changeIndex = (lYearIndex!! + lMonthIndex!! + lDayIndex!! + timeIndex) % 6
            // 除尽取6
            if (changeIndex == 0) {
                changeIndex = 6
            }

            // 由于是从下往上数, 所以得转换下
            val formatChangeIndex = 6 - changeIndex
            var changeString = ""
            if (fullGuaNum[formatChangeIndex].toString() == "1") {
                changeString = "0"
            } else {
                changeString = "1"
            }

            // 变卦数字
            val afterChangeGuaNum = fullGuaNum.replaceRange(formatChangeIndex, formatChangeIndex + 1, changeString)
            // 获取变卦
            val changeGua = gua64[afterChangeGuaNum]
            // 获取主卦
            val mainGua = gua64[fullGuaNum]

            mTickGuaTextPaint.textSize = 24f
            mTickGuaTextPaint.textAlign = Paint.Align.CENTER
            canvas.save()
            canvas.translate(mCenterX, mCenterY)
            var timeStr = addZero(mCalendar.get(Calendar.HOUR_OF_DAY)) + ":" + addZero(mCalendar.get(Calendar.MINUTE)) + "  " + getTimeStr()
            canvas.drawText(timeStr, 0f, -mCenterY / 2f, mTickGuaTextPaint)
            var dateStr = cd.get(Calendar.YEAR).toString() + "年" + (cd.get(Calendar.MONTH) + 1) + "月" + cd.get(Calendar.DATE) + "日"  + ' ' + lunarObj?.ncWeek
            canvas.drawText(dateStr, 0f, -mCenterY / 3f, mTickGuaTextPaint)
            var lunarStr = lunarObj?.gzYear + "年 " + lunarObj?.Animal + ' ' + lunarObj?.IMonthCn + lunarObj?.IDayCn
            canvas.drawText(lunarStr, 0f, -mCenterY / 5.5f, mTickGuaTextPaint)

            canvas.drawText("主卦：$mainGua", 0f, mCenterY / 3f, mTickGuaTextPaint)

            canvas.drawText("变卦：$changeGua", 0f, mCenterY / 2f, mTickGuaTextPaint)
            canvas.restore()
        }

        private fun addZero(num: Int): String {
            if (num < 10) {
                return "0$num"
            } else {
                return num.toString()
            }
        }

        private fun getTimeIndex(): Int {
            var hour = mCalendar.get(Calendar.HOUR_OF_DAY)

            return Math.floor((((hour+1)%24)/2).toDouble()).toInt() + 1
        }

        private fun getTimeStr(): String {
            var hour = mCalendar.get(Calendar.HOUR_OF_DAY)

            return "子丑寅卯辰巳午未申酉戌亥"[floor((((hour+1)%24)/2).toDouble()).toInt()] + "时"
        }


        private fun drawWatchFace(canvas: Canvas) {
            /*
             * Draw ticks. Usually you will want to bake this directly into the photo, but in
             * cases where you want to allow users to select their own photos, this dynamically
             * creates them on top of the photo.
             */
            mTickLunarTextPaint.textSize = 20f
            mTickLunarTextPaint.textAlign = Paint.Align.CENTER
            mTickLunarTextPaint.color = Color.parseColor("#E6A23C")

            mTickHourTextPaint.textSize = 14f
            mTickHourTextPaint.textAlign = Paint.Align.CENTER
            for (tickIndex in 0..23) {
                canvas.save()
                canvas.translate(mCenterX, mCenterY)
                canvas.rotate((15.toDouble() * tickIndex).toFloat())
                if (tickIndex % 2 == 0) {
                    canvas.drawText(cTimeArr[tickIndex / 2], 0f, -mCenterY/ 1.15f, mTickLunarTextPaint)
                } else {
                    canvas.drawText("|", 0f, -mCenterY/ 1.1f, mTickLunarTextPaint)
                }
                canvas.restore()
            }

            for (tickIndex in 0..23) {
                canvas.save()
                canvas.translate(mCenterX, mCenterY)
                var index = tickIndex.toString()
                if (tickIndex == 0) {
                    index = "24"
                } else if (tickIndex < 10) {
                    index = "$index"
                }

                canvas.rotate((15.toDouble() * tickIndex).toFloat())
                canvas.drawText(index.toString(),
                    0f, -mCenterY/1.3f , mTickHourTextPaint)
                canvas.restore()
            }

            mDirectionPaint.textSize = 20f
            mDirectionPaint.textAlign = Paint.Align.CENTER
            mDirectionPaint.color = Color.parseColor("#67C23A")
            var directionArr = arrayListOf("坎","艮","震","巽","离","坤","兑","乾")
            for (tickIndex in 0..7) {
                canvas.save()
                canvas.translate(mCenterX, mCenterY)
                canvas.rotate((45.toDouble() * tickIndex).toFloat() + sensorRotate)
                canvas.drawText(directionArr[tickIndex],
                    0f, -mCenterY/1.3f , mDirectionPaint)
                canvas.restore()
            }
            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            val seconds =
                mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f
            val secondsRotation = seconds * 6f

            val minutesRotation = mCalendar.get(Calendar.MINUTE) * 6f
            val hourHandOffset = mCalendar.get(Calendar.MINUTE) / 4f
            val hoursRotation = mCalendar.get(Calendar.HOUR_OF_DAY) * 15 + hourHandOffset

            /*
             * Save the canvas state before we can begin to rotate it.
             */
            canvas.save()

            canvas.rotate(hoursRotation, mCenterX, mCenterY)
            canvas.drawLine(
                mCenterX,
                mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                mCenterX,
                mCenterY - sHourHandLength,
                mHourPaint
            )

            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY)
            canvas.drawLine(
                mCenterX,
                mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                mCenterX,
                mCenterY - sMinuteHandLength,
                mMinutePaint
            )

            /*
             * Ensure the "seconds" hand is drawn only when we are in interactive mode.
             * Otherwise, we only update the watch face once a minute.
             */
            if (!mAmbient) {
                canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY)
                canvas.drawLine(
                    mCenterX,
                    mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                    mCenterX,
                    mCenterY - mSecondHandLength,
                    mSecondPaint
                )

            }

            canvas.drawCircle(
                mCenterX,
                mCenterY,
                CENTER_GAP_AND_CIRCLE_RADIUS,
                mTickLunarTextPaint
            )

            /* Restore the canvas" original orientation. */
            canvas.restore()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerReceiver()
                /* Update time zone in case it changed while we weren"t visible. */
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            } else {
                unregisterReceiver()
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer()
        }

        private fun registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            this@MyWatchFace.registerReceiver(mTimeZoneReceiver, filter)
        }

        private fun unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = false
            this@MyWatchFace.unregisterReceiver(mTimeZoneReceiver)
        }

        /**
         * Starts/stops the [.mUpdateTimeHandler] timer based on the state of the watch face.
         */
        private fun updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            }
        }

        /**
         * Returns whether the [.mUpdateTimeHandler] timer should be running. The timer
         * should only run in active mode.
         */
        private fun shouldTimerBeRunning(): Boolean {
            return isVisible && !mAmbient
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        fun handleUpdateTimeMessage() {
            invalidate()
            if (shouldTimerBeRunning()) {
                val timeMs = System.currentTimeMillis()
                val delayMs = INTERACTIVE_UPDATE_RATE_MS - timeMs % INTERACTIVE_UPDATE_RATE_MS
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
            }
        }
    }
}