package com.atlas.mars.glidecon.model

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.atlas.mars.glidecon.R
import kotlin.math.cos
import kotlin.math.sin

class DashboardDrawer (private val context: Context, val size: Int){
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
        p.strokeWidth = density * 5
        val matrix = Matrix()


        val limit = (90*speed/SPEED_MAX).toInt()

        for(i in 0..limit step 2){
            path.reset()
            path.moveTo(0.0f, size.toFloat())
            path.lineTo(0.0f + density*30, size.toFloat())

            matrix.reset()
            matrix.setRotate(i.toFloat(), size.toFloat(), size.toFloat());
            path.transform(matrix);
            canvas.drawPath(path, p)
        }
    }

    private fun clear(){
        val clearPaint = Paint()
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), clearPaint)
    }

    private fun drawMeasurement(){
        path.reset()
        val p = Paint()
        path.moveTo(0.0f, size.toFloat())
        path.lineTo(0.0f + density*20, size.toFloat())
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.alpha = 50
        p.strokeWidth = density * 5
        canvas.drawPath(path, p)
        val matrix = Matrix()
        for(i in 0..90 step 2){
            path.reset()
            path.moveTo(0.0f, size.toFloat())
            path.lineTo(0.0f + density*30, size.toFloat())

            matrix.reset()
            matrix.setRotate(i.toFloat(), size.toFloat(), size.toFloat());
            path.transform(matrix);
            canvas.drawPath(path, p)
        }
    }
}