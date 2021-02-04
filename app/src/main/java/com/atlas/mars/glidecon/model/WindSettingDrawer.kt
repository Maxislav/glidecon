package com.atlas.mars.glidecon.model

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.core.content.ContextCompat
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.dialog.DialogWindSetting
import kotlin.math.cos
import kotlin.math.sin

class WindSettingDrawer(private val context: Context, private var size: Number, var maxSpeed: Double) {
    val TAG = "WindSettingDrawer"
    var bitmap: Bitmap = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
    var canvas: Canvas = Canvas(bitmap)
    private val density: Float = Density(context).density
    val path = Path()

    init {
        drawMeasurement()
    }

    fun drawByXy(x: Float, y: Float){
        val size = this.size.toFloat()
        val clearPaint = Paint()
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRect(0f, 0f, size, size, clearPaint)
        drawMeasurement()
        val p = Paint()
        p.style = Paint.Style.STROKE
        p.strokeWidth = density * 4
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        path.reset()
        path.moveTo(size / 2, size / 2)
        path.lineTo(x, y)
        canvas.drawPath(path, p)
    }

    fun draw(direction: Double, speed: Double) {
        val size = this.size.toFloat()
        val clearPaint = Paint()
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRect(0f, 0f, size, size, clearPaint)
        drawMeasurement()

        val p = Paint()
        p.style = Paint.Style.STROKE
      //  p.alpha = 180
        p.strokeWidth = density * 4
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        path.reset()
        path.moveTo(size / 2, size / 2)

        // 30  = size/2
        // speed = x

        val scale = maxSpeed / (size/2)



        val r: Double =  speed * (size/2) / maxSpeed.toFloat()/// scale.toFloat()

       //  Log.d(TAG, "radius $r")

        val dx = Math.sin(Math.toRadians(direction)) * r
        val dy = Math.cos(Math.toRadians(direction)) * r

        val x = (size/2 + dx).toFloat()
        val y =  (size/2 - dy).toFloat()
        path.lineTo(x, y)
        // Log.d(TAG, "rxy from draw $r , - $x $y")
       //  Log.d(TAG, "drawer radius $r")
        Log.d(TAG, "drawer X $x")


        canvas.drawPath(path, p)
        // sin = x/r
    }

    private fun drawMeasurement() {
        path.reset()
        val size = this.size.toFloat()
        var r = size / 2
        val p = Paint()
        for (i in 0..360 step 5) {
            path.moveTo(size / 2, size / 2)
            val x = size / 2 + r * sin(i * Math.PI / 180)
            val y = size / 2 - r * cos(i * Math.PI / 180)
            path.lineTo(x.toFloat(), y.toFloat())
        }

        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
         p.alpha = 180
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.strokeWidth = density * 2
        canvas.drawPath(path, p)



        path.reset()
        path.addCircle(size / 2, size / 2, size / 2 - density * 5, Path.Direction.CCW)
        path.close()
        p.style = Paint.Style.FILL
        p.alpha = 180
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawPath(path, p)

        path.reset()
        p.reset()
        for (i in 0..360 step 15) {
            path.moveTo(size / 2, size / 2)
            val x = size / 2 + r * sin(i * Math.PI / 180)
            val y = size / 2 - r * cos(i * Math.PI / 180)
            path.lineTo(x.toFloat(), y.toFloat())
        }
        p.strokeWidth = density * 4
        p.style = Paint.Style.STROKE
        canvas.drawPath(path, p)


        path.reset()
        p.reset()
        path.addCircle(size / 2, size / 2, size / 2 - density * 20, Path.Direction.CCW)
        path.close()
        p.style = Paint.Style.FILL
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawPath(path, p)
    }


}