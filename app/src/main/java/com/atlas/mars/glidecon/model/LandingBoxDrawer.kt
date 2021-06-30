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
        val size = this.size.toFloat()
        val clearPaint = Paint()
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRect(0f, 0f, size, size, clearPaint)
        drawMeasurement()

        var matrix = Matrix()
        var paint = Paint()
        var path = getRectPath(paint)

        matrix.reset()
        matrix.setTranslate(size/2, size/2 - size/5)
        path.transform(matrix)
        matrix.reset()
        matrix.setRotate(direction.toFloat(), size/2, size/2)
        path.transform(matrix)
        canvas.drawPath(path, paint)

        matrix.reset()
        matrix.setTranslate(size/2, size - size/5)
        path = getRectPath(paint)
        path.transform(matrix)
        matrix.reset()
        matrix.setRotate(direction.toFloat(), size/2, size/2)
        path.transform(matrix)
        canvas.drawPath(path, paint)

        matrix.reset()
        matrix.setTranslate(size/2, size - size/5f - size/5)
        path = getRectPath(paint)
        path.transform(matrix)
        matrix.reset()
        matrix.setRotate(direction.toFloat(), size/2, size/2)
        path.transform(matrix)
        canvas.drawPath(path, paint)


        matrix.reset()
        matrix.setTranslate(size/2 - size/6 , size/2 - size/5)
        path = getRectPath(paint)
        path.transform(matrix)
        matrix.reset()
        matrix.setRotate(direction.toFloat(), size/2, size/2)
        path.transform(matrix)
        canvas.drawPath(path, paint)

        matrix.reset()
        matrix.setTranslate(size/2 - size/6 - size/6, size/2 - size/5)
        path = getRectPath(paint)
        path.transform(matrix)
        matrix.reset()
        matrix.setRotate(direction.toFloat(), size/2, size/2)
        path.transform(matrix)
        canvas.drawPath(path, paint)


        /*val p = Paint()
        p.style = Paint.Style.STROKE
        p.strokeWidth = density * 4
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.alpha = 150
        path.reset()

        matrix.setRotate(direction.toFloat() + 180, size/2, size/2)
        path.moveTo(size / 2, size / 2 + size/10)
        path.lineTo(size / 2 + size/4, size / 2 + size/10)
        path.lineTo(size / 2 + size/4, size / 2 - size/3)
        path.lineTo(size / 2, size / 2 - size/3)
        path.lineTo(size / 2, size / 2 - size/20)
        path.moveTo(size / 2 - size/20, size / 2 - size/20 )
        path.lineTo(size / 2, size / 2 )
        path.lineTo(size / 2 + size/20, size / 2 - size/20  )

        path.transform(matrix);
        canvas.drawPath(path, p)
*/
        // canvas.tran
    }

    private fun getRectPath(p: Paint): Path{
        val size = this.size.toFloat()
        val path = Path()
        p.style = Paint.Style.FILL
        p.color = ContextCompat.getColor(context, R.color.white)
        //path.moveTo(0.0f, 0.0f)
        val rect = RectF(0.0f, 0.0f, size/30 , size/10)
        path.addRect(rect, Path.Direction.CW)
        //path.quadTo(0.0f, 0.0f, size/20 , size/10)
        return path
    }

    private fun drawMeasurement() {
        path.reset()
        val size = this.size.toFloat()
        val r = size / 2

        /*var paintGreen =  Paint()
        var pathGreen  = Path()
        paintGreen.style = Paint.Style.FILL
        paintGreen.color = ContextCompat.getColor(context, R.color.green)
        pathGreen.addCircle(size / 2, size / 2, size / 2 , Path.Direction.CW)
        canvas.drawPath(pathGreen, paintGreen)*/
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
        path.addCircle(size / 2, size / 2, size / 2, Path.Direction.CCW)
        path.close()
        p.color = ContextCompat.getColor(context, R.color.green)
        p.style = Paint.Style.FILL
        //p.alpha = 5
        //p.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
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
        p.color = ContextCompat.getColor(context, R.color.green)
        p.style = Paint.Style.FILL
        // p.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawPath(path, p)


        path.reset()
        p.reset()
        path.addCircle(size / 2, size / 2, density * 10, Path.Direction.CCW)
        path.close()
        // p.alpha = 80
        p.strokeWidth = density * 4
        p.style = Paint.Style.STROKE
        canvas.drawPath(path, p)
    }
}