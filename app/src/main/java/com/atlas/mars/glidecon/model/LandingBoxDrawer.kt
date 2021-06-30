package com.atlas.mars.glidecon.model

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.atlas.mars.glidecon.R
import kotlin.math.cos
import kotlin.math.sin

class LandingBoxDrawer(private val context: Context, private var size: Number) {
    private val TAG = "LandingBoxDrawer"
    var bitmap: Bitmap = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
    var canvas: Canvas = Canvas(bitmap)
    private val density: Float = Density(context).density
    val path = Path()

    init {
        drawMeasurement()
    }
    fun draw(direction: Double){

    }

    private fun drawMeasurement() {
        path.reset()
        val size = this.size.toFloat()
        val r = size / 2
        val p = Paint()
        for (i in 0..360 step 5) {
            path.moveTo(size / 2, size / 2)
            val x = size / 2 + r * sin(i * Math.PI / 180)
            val y = size / 2 - r * cos(i * Math.PI / 180)
            path.lineTo(x.toFloat(), y.toFloat())
        }

        p.isAntiAlias = true
        p.style = Paint.Style.STROKE

        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.alpha = 80
        p.strokeWidth = density * 2
        canvas.drawPath(path, p)



        path.reset()
        path.addCircle(size / 2, size / 2, size / 2 - density * 5, Path.Direction.CCW)
        path.close()
        p.style = Paint.Style.FILL
        p.alpha = 5
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
        p.alpha = 80
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


        path.reset()
        p.reset()
        path.addCircle(size / 2, size / 2, density * 10, Path.Direction.CCW)
        path.close()
        p.alpha = 80
        p.strokeWidth = density * 4
        p.style = Paint.Style.STROKE
        canvas.drawPath(path, p)
    }
}