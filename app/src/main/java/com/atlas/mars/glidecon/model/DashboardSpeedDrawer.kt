package com.atlas.mars.glidecon.model

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.atlas.mars.glidecon.R
import kotlin.math.cos
import kotlin.math.sin

class DashboardSpeedDrawer (private val context: Context, val size: Int){
    var bitmap: Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    var canvas: Canvas = Canvas(bitmap)
    private val density: Float = Density(context).density
    val path = Path()

    companion object{
       const  val TAG = "DashboardDrawer"
        const val SPEED_MAX = 200.0f
    }
    init {

    }

    fun setSpeed(speed: Float){
        clear()
        drawMeasurement()
        val p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.alpha = 255
        p.strokeWidth = density * 2
        val matrix = Matrix()
        val limit = (1000*speed/SPEED_MAX).toInt()

        for (i in 0 until limit - 1 step 5) {
            path.reset()
            path.moveTo(size.toFloat()/2, 0.0f)
            path.lineTo(size.toFloat()/2 , 0.0f + density*20)

            matrix.reset()
            val a: Float = (i*360/ DashboardAltitudeDrawer.ALT_MAX).toFloat()
            matrix.setRotate(a, size.toFloat()/2, size.toFloat()/2);
            path.transform(matrix);
            canvas.drawPath(path, p)
        }
       /* for(i in 0..limit step 2){
            path.reset()
            path.moveTo(size.toFloat()/2, 0.0f)
            path.lineTo(size.toFloat()/2 , 0.0f + density*20)

            matrix.reset()
            matrix.setRotate(i.toFloat(), size.toFloat()/2, size.toFloat()/2);
            path.transform(matrix);
            canvas.drawPath(path, p)
        }*/
    }

    private fun clear(){
        val clearPaint = Paint()
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), clearPaint)
    }

    private fun drawMeasurement(){
        path.reset()
        val p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.alpha = 50
        p.strokeWidth = density * 2
        canvas.drawPath(path, p)
        val matrix = Matrix()

        for (i in 0 until 1000 - 1 step 10) {
            path.reset()
            path.moveTo(size.toFloat()/2, 0.0f)
            path.lineTo(size.toFloat()/2 , 0.0f + density*15)

            matrix.reset()
            val a: Float = (i*360/ DashboardAltitudeDrawer.ALT_MAX).toFloat()
            matrix.setRotate(a, size.toFloat()/2, size.toFloat()/2);
            path.transform(matrix);
            canvas.drawPath(path, p)
        }
        /*for(i in 0 until 360-1 step 5){
            path.reset()
            path.moveTo(size.toFloat()/2, 0.0f)
            path.lineTo(size.toFloat()/2 , 0.0f + density*15)

            matrix.reset()
            matrix.setRotate(i.toFloat(), size.toFloat()/2, size.toFloat()/2);
            path.transform(matrix);
            canvas.drawPath(path, p)
        }*/
    }
}