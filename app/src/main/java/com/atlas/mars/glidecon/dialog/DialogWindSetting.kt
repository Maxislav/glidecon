package com.atlas.mars.glidecon.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.*
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.widget.FrameLayout
import android.widget.ImageView
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.model.WindSettingDrawer
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.windSubject
import com.atlas.mars.glidecon.view.CustomFontTextView
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import java.text.DecimalFormat
import kotlin.math.atan
import kotlin.math.pow


class DialogWindSetting(val activity: Activity) : AlertDialog.Builder(activity) {
    private val TAG = "DialogWindSetting"
    lateinit var alertDialog: AlertDialog
    lateinit var linearLayout: FrameLayout
    lateinit var deviceBitmap: Bitmap
    private val imageSizeSubject = BehaviorSubject.create<Number>()
    private val touchPositionSubject = BehaviorSubject.create<Pair<Float, Float>>()
    private lateinit var windSettingDrawer: WindSettingDrawer
    lateinit var imageView: ImageView
    var isSubscribe = true;
    var directionSpeed: Pair<Double, Double> = Pair(0.0, 0.0)

    val directionView: CustomFontTextView
    val speedView: CustomFontTextView
    private var handler: Handler

    private val screenWidth: Int
        get() {
            val displayRectangle = Rect()
            dialogWindow?.decorView?.getWindowVisibleDisplayFrame(displayRectangle);
            return displayRectangle.width()
        }

    private val screenHeight: Int
        get() {
            val displayRectangle = Rect()
            dialogWindow?.decorView?.getWindowVisibleDisplayFrame(displayRectangle);
            return displayRectangle.height()
        }

    private val dialogWindow: Window?
        get() {
            return alertDialog.window
        }

    init {
        val inflater = LayoutInflater.from(context)
        linearLayout = inflater.inflate(R.layout.dialog_wind_setting, null, false) as FrameLayout
        setView(linearLayout)
        setPositiveButton("Save", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                Log.d(TAG, "Save")
                onDestroy()

                directionSpeed
                windSubject.onNext(mapOf(MapBoxStore.Wind.DIRECTION to directionSpeed.first, MapBoxStore.Wind.SPEED to directionSpeed.second))
            }
        })
        directionView = linearLayout.findViewById(R.id.direction_view)
        speedView = linearLayout.findViewById(R.id.speed_view)
        handler = object : Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            override fun handleMessage(msg: Message) {
                directionSpeed = msg.obj as Pair<Double, Double>
                setViewParams()

                imageView.setImageBitmap(windSettingDrawer.bitmap)
            }
        }

        setNegativeButton("Cancel", null)


        imageSizeSubject
                .take(1)
                .doOnNext { size ->
                    windSettingDrawer = WindSettingDrawer(context, size, MAX_WIND_SPEED)
                    imageView.setImageBitmap(windSettingDrawer.bitmap)
                }
                .flatMap { windSubject }
                .takeWhile { isSubscribe }
                .subscribeBy { wind ->
                    Log.d(TAG, "")
                    val direction = wind.get(MapBoxStore.Wind.DIRECTION)?.toDouble()
                    val speed = wind.get(MapBoxStore.Wind.SPEED)?.toDouble()
                    if (direction != null && speed !== null) {
                        windSettingDrawer.draw(direction, speed)
                        directionSpeed = Pair(direction, speed)
                        setViewParams()
                    }
                }
        Observables.combineLatest(imageSizeSubject, touchPositionSubject)
                .takeWhile { isSubscribe }
                // .sample(100, TimeUnit.MILLISECONDS)
                .subscribeBy { pair ->

                    // windSubject.onNext(mapOf(MapBoxStore.Wind.DIRECTION to 1, MapBoxStore.Wind.SPEED to 5))

                    val size = pair.first.toFloat()
                    val x = pair.second.first
                    val y = pair.second.second
                    var a = 0.0
                    a = Math.toDegrees(atan((x - size / 2) / (y - size / 2)).toDouble())
                    if (size / 2 < x && y < size / 2) {
                        (-a).also { a = it };
                    } else if (size / 2 < x && y < size) {
                        a = 180 - a
                    } else if (x < size / 2 && size / 2 < y) {
                        a = 180 - a
                    } else {
                        a = 360 - a
                    }
                    val x1 = size / 2
                    val y1 = size / 2

                    val sum = (x - x1).toDouble().pow(2.0) + (y - y1).toDouble().pow(2.0)
                    val distInPx = sum.pow(0.5)
                    val speed: Double = distInPx * MAX_WIND_SPEED / (size / 2)
                    Log.d(TAG, "dialog X $x")
                    windSettingDrawer.draw(a, speed)
                    val msg = handler.obtainMessage(1, Pair(a, speed))
                    handler.sendMessage(msg)

                }
    }

    private fun setViewParams(){
        val direction = DecimalFormat("#").format(directionSpeed.first)
        val speed = DecimalFormat("#").format(directionSpeed.second)

        directionView.text = "$direction \u00B0"
        speedView.text = "$speed"
    }

    private fun onDestroy() {
        isSubscribe = false
        handler.removeCallbacksAndMessages(null);
    }

    @SuppressLint("ResourceAsColor")
    override fun create(): AlertDialog {
        alertDialog = super.create()
        alertDialog.setOnDismissListener {
            onDestroy()
        }

        imageView = linearLayout.findViewById(R.id.wind_image_view)
        val size = Math.min(screenWidth, screenHeight)
        imageView.layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        imageView.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT

        imageView.post(Runnable {
            val size1 = imageView.width
            imageView.layoutParams.height = size1
            imageView.layoutParams.width = size1
            initTouchListener(imageView)
        })
        alertDialog.setOnShowListener(DialogInterface.OnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.colorPrimaryText);
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.colorPrimaryText);
        })

        val windowDialog: Window? = alertDialog.getWindow()

        windowDialog?.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)

        val layoutParams = windowDialog?.attributes
        layoutParams?.y = 100
        layoutParams?.x = 0
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        windowDialog?.attributes = layoutParams
        return alertDialog
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initTouchListener(imageView: ImageView) {
        val width = imageView.layoutParams.width
        val height = imageView.layoutParams.height
        imageSizeSubject.onNext(Math.min(width, height))

        imageView.setOnTouchListener(object : View.OnTouchListener {

            val g = 0
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val X: Float? = event?.x
                val Y: Float? = event?.y
                when (event!!.action and MotionEvent.ACTION_MASK) {
                    ACTION_DOWN, ACTION_MOVE -> {
                        if (X != null && Y != null) {
                            touchPositionSubject.onNext(Pair(X, Y))
                        }
                    }
                }
                return true
            }
        })
    }

    companion object {
        val MAX_WIND_SPEED = 20.0
    }
}