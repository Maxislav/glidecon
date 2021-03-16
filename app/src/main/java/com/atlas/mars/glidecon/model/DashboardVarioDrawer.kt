package com.atlas.mars.glidecon.model

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.atlas.mars.glidecon.R

class DashboardVarioDrawer(private val context: Context, val size: Int) {
    var bitmap: Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    var canvas: Canvas = Canvas(bitmap)
    private val density: Float = Density(context).density
    val path = Path()

    companion object {
        const val TAG = "DashboardDrawer"
        const val VARIO_MAX = 5.0f
    }

    init {

    }

    fun setVario(vario: Float) {
        clear()
        drawMeasurement()
        val p = Paint()
        p.isAntiAlias = true

        p.style = Paint.Style.STROKE
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.alpha = 255
        p.strokeWidth = density * 2
        val matrix = Matrix()
        val limit = (250 * vario / VARIO_MAX).toInt()
        val ccv = vario < 0;
        for (i in 0 until Math.abs(limit) step 5) {
            path.reset()
            path.moveTo(0.0f, size.toFloat() / 2)
            path.lineTo(density * 20, size.toFloat() / 2)

            matrix.reset()
            val a: Float = (i * 360 / 1000.0f).toFloat()
            matrix.setRotate(ccv.let { if (it) -a else a }, size.toFloat() / 2, size.toFloat() / 2);
            path.transform(matrix);
            canvas.drawPath(path, p)
        }

        /*
        for(i in 0..Math.abs(limit) step 2){
            path.reset()
            path.moveTo(0.0f , size.toFloat()/2)
            path.lineTo(density*30 , size.toFloat()/2  )

            matrix.reset()
            matrix.setRotate(ccv.let { if(it) -i.toFloat() else i.toFloat()}, size.toFloat()/2, size.toFloat()/2);
            path.transform(matrix);
            canvas.drawPath(path, p)
        }*/
    }

    private fun clear() {
        val clearPaint = Paint()
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), clearPaint)
    }

    private fun drawMeasurement() {
        path.reset()
        val p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.alpha = 100
        p.strokeWidth = density * 4
        path.reset()
        path.moveTo(0.0f, size.toFloat() / 2)
        path.lineTo(density * 20, size.toFloat() / 2)
        canvas.drawPath(path, p)
        val matrix = Matrix()
        p.strokeWidth = density * 2
        p.alpha = 50
        for (i in 0 until 1000 - 1 step 10) {
            path.reset()
            path.moveTo(0.0f, size.toFloat() / 2)
            path.lineTo(density * 15, size.toFloat() / 2)

            matrix.reset()
            val a: Float = (i * 360 / 1000.0f).toFloat()
            matrix.setRotate(a, size.toFloat() / 2, size.toFloat() / 2);
            path.transform(matrix);
            canvas.drawPath(path, p)
        }
    }
}