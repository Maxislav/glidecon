package com.atlas.mars.glidecon.model

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.atlas.mars.glidecon.R

class DashboardAltitudeDrawer (private val context: Context, val size: Int){
    var bitmap: Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    var canvas: Canvas = Canvas(bitmap)
    private val density: Float = Density(context).density
    val path = Path()

    companion object{
        const  val TAG = "DashboardAltitudeDrawer"
        const val ALT_MAX = 1000.0f
    }
    init {

    }

    fun setAlt(alt: Float){
        clear()
        drawMeasurement()
        val p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.alpha = 255
        p.strokeWidth = density * 3
        val matrix = Matrix()

        val limit = ((alt+ALT_MAX)%ALT_MAX).toInt()
        for(i in 0 until limit step 20){
            path.reset()
            path.moveTo(size.toFloat()/2, 0.0f)
            path.lineTo(size.toFloat()/2 , 0.0f + density*20)

            matrix.reset()
            val a: Float = (i*360/ALT_MAX).toFloat()
            matrix.setRotate(a, size.toFloat()/2, size.toFloat()/2);
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
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.alpha = 50
        p.strokeWidth = density * 2
        canvas.drawPath(path, p)
        val matrix = Matrix()
        for(i in 0 until ALT_MAX.toInt()-1 step 20){
            path.reset()
            path.moveTo(size.toFloat()/2, 0.0f)
            path.lineTo(size.toFloat()/2 , 0.0f + density*15)

            matrix.reset()
            val a: Float = (i*360/ALT_MAX).toFloat()
            matrix.setRotate(a, size.toFloat()/2, size.toFloat()/2);
            path.transform(matrix);
            canvas.drawPath(path, p)
        }
    }
}