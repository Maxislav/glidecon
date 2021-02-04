package com.atlas.mars.glidecon.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.*
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.model.MyImage
import com.atlas.mars.glidecon.model.WindSettingDrawer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import java.util.Calendar.MILLISECOND
import java.util.concurrent.TimeUnit
import kotlin.math.atan
import kotlin.math.pow


class DialogWindSetting(val activity: Activity) : AlertDialog.Builder(activity) {
    private val TAG = "DialogWindSetting"
    lateinit var alertDialog: AlertDialog
    lateinit var linearLayout: RelativeLayout
    lateinit var deviceBitmap: Bitmap
    private val imageSizeSubject = BehaviorSubject.create<Number>()
    private val touchPositionSubject = BehaviorSubject.create<Pair<Float, Float>>()
    private lateinit var windSettingDrawer: WindSettingDrawer
    lateinit var imageView: ImageView
    var isSubscribe = true;
    private lateinit var emergencyHandler: Handler

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
        linearLayout = inflater.inflate(R.layout.dialog_wind_setting, null, false) as RelativeLayout
        setView(linearLayout)
        setPositiveButton("ok", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                Log.d(TAG, "ok")
            }
        })
        emergencyHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                imageView.setImageBitmap(windSettingDrawer.bitmap)
                // this@LocationService.stopSelf()
            }
        }

        setNegativeButton("cancel", null)
        imageSizeSubject
                .take(1)
                .subscribeBy { size ->
                    windSettingDrawer = WindSettingDrawer(context, size, MAX_WIND_SPEED)
                    imageView.setImageBitmap(windSettingDrawer.bitmap)

                }
        Observables.combineLatest(imageSizeSubject, touchPositionSubject)
                .takeWhile { isSubscribe }
                .sample(200, TimeUnit.MILLISECONDS)
                .subscribeBy { pair ->
                    // Log.d(TAG, "${pair.first} ${pair.second}")
                    val size = pair.first.toFloat()
                    val x = pair.second.first
                    val y = pair.second.second
                    var a: Double = 0.0
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
                    // Log.d(TAG, "dist px -> $distInPx")
                    //val speed =  distInPx * MAX_WIND_SPEED / (size/2)

                    // max - size/2
                    // x = dist
                    val speed: Double = distInPx * MAX_WIND_SPEED/ (size/2)

                    // speed = distx*k   distx = speed/k
                    // Log.d(TAG, "$a speed = $speed")
                    // windSettingDrawer.drawByXy(x, y);
                    // Log.d(TAG, "xt from dialog $x $y")
                    //Log.d(TAG, "size=${size/2},  distInPx=$distInPx, speed = $speed, angle= $a, xy = $x $y")
                    // Log.d(TAG, "dialog radius $distInPx ")
                    Log.d(TAG, "dialog X $x")
                     windSettingDrawer.draw(a, speed)
                   // windSettingDrawer.drawByXy(x, y)
                    emergencyHandler.sendEmptyMessage(1)

                    // size = max_w
                    // distpx = x

                }
    }

    @SuppressLint("ResourceAsColor")
    override fun create(): AlertDialog {
        alertDialog = super.create()
        alertDialog.setOnDismissListener {
            isSubscribe = false
            emergencyHandler.removeCallbacksAndMessages(null);
        }

        imageView = linearLayout.findViewById(R.id.wind_image_view)
        val size = Math.min(screenWidth, screenHeight)
        imageView.layoutParams.height = size
        imageView.layoutParams.width = size
        alertDialog.setOnShowListener(DialogInterface.OnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.colorPrimaryText);
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.colorPrimaryText);

            val size1 = Math.min(linearLayout.width, linearLayout.height)
            imageView.layoutParams.height = size1
            imageView.layoutParams.width = size1

            initTouchListener(imageView)
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


                        /* Log.d(TAG, "$X $Y")
                         canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), clearPaint)

                         path.reset()
                         path.moveTo(width.toFloat() / 2, height.toFloat() / 2)
                         if (X != null && Y != null) {
                             path.lineTo(X.toFloat(), Y.toFloat())
                         }
                         var p = Paint()
                         p.isAntiAlias = true
                         p.style = Paint.Style.STROKE
                         p.alpha = 180
                         p.strokeWidth = 10.0f
                         p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
                         canvas.drawPath(path, p)
                         imageView.setImageBitmap(deviceBitmap)*/
                    }
                }

                return true

            }
        })
    }

    companion object {
        val MAX_WIND_SPEED = 30.0
    }
}