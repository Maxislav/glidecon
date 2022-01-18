package com.atlas.mars.glidecon.model

import android.content.Context
import android.graphics.*
import android.util.Log
import com.atlas.mars.glidecon.R

class ImageBikeComputer(var context: Context) {
    val density: Float =  Density(context).density
    var bitmap: Bitmap
    var widthDp: Int
    var heightDp: Int
    var widthPx: Int
    var heightPx: Int
    var canvas: Canvas
    init {
        widthDp = 200
        heightDp = 100
        widthPx = Math.round(widthDp * density)
        heightPx = Math.round(heightDp * density)
        bitmap = Bitmap.createBitmap(widthPx, widthPx, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)


        //canvas.scale(1,0.5f);
    }

    fun getBitmap(speed: Double): Bitmap {
        val strokeWidth = 20f
        val paintClear = Paint()
        paintClear.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        val rect = Rect(0, 0, widthPx, heightPx)
        canvas.drawRect(rect, paintClear)
        val oval = RectF()
        //oval.set(20*density/2, 20*density/2, (float)((widthPx*2) - (20*density) ), (float)((widthPx*2) - (20*density) ));
        oval[strokeWidth * density / 2, strokeWidth * density / 2, widthPx * 2 - strokeWidth * density] = widthPx * 2 - strokeWidth * density
        val myPath = Path()
        val myPath2 = Path()
        val stepAngle = (90.0 / 50.0).toFloat()
        val paint1 = Paint()
        paint1.reset()
        paint1.strokeWidth = strokeWidth * density
        paint1.isAntiAlias = true
        paint1.style = Paint.Style.STROKE
        paint1.color = context.resources.getColor(R.color.colorPrimaryText)
        val paint2 = Paint()
        paint2.reset()
        paint2.strokeWidth = strokeWidth * density
        paint2.isAntiAlias = true
        paint2.style = Paint.Style.STROKE
        paint2.color = context.resources.getColor(R.color.colorPrimaryDarkWhite)
        for (i in (speed/4).toInt()..50) {
            myPath.arcTo(oval, -180 + i * stepAngle, stepAngle - 0.7f, true)
            canvas.drawPath(myPath, paint2)
            //Log.d("LOG_angle", -180 + i * stepAngle + "")
        }
        var i = 0
        while (i < speed/4) {
            myPath2.arcTo(oval, -180 + i * stepAngle, stepAngle - 0.7f, true)
            canvas.drawPath(myPath2, paint1)
            //Log.d("LOG_angle", -180 + i * stepAngle + "")
            i++
        }
        val m = Matrix()
        m.setScale(1f, 0.5f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }


}
