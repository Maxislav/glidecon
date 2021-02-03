package com.atlas.mars.glidecon.model

import android.content.Context
import android.graphics.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.atlas.mars.glidecon.R
import kotlin.math.cos
import kotlin.math.sin

class MyImage(var context: Context) {
    var density: Float = Density(context).density
    val arrow: Bitmap
        get() = createArrow(R.color.colorPrimaryDark)
    val btnArrowTypical: Bitmap
        get() = createBtnArrow(R.color.colorPrimary, 1.0f)
    val btnArrowFollow: Bitmap
        get() = createBtnArrow(R.color.colorPrimary, 3.0f)
    val btnArrowFollowRotate: Bitmap
        get() = createBtnArrow(R.color.colorPrimaryDark, 3f)
    val btnCompass: Bitmap
        get() = createBtnCompass()
    val btnZoomIn: Bitmap
        get() = createBtnZoom("+")
    val btnZoomOut: Bitmap
        get() = createBtnZoom("-")

    /*val btnTrack: Bitmap
        get() = createBtnTrack()*/
    val btnWind: Bitmap
        get() = createBtnWind()



    fun getManeuverArrow(@DrawableRes resId: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(Math.round(30 * density), Math.round(30 * density), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val icon = BitmapFactory.decodeResource(context.resources, resId)
        val scaleArrow = Bitmap.createScaledBitmap(icon, Math.round(40 * density / 1.5).toInt(), Math.round(40 * density / 1.5).toInt(), true)
        canvas.drawBitmap(scaleArrow, 0f, 0f, null)
        return bitmap
    }

    fun getImageGpsStat(all: Int, fix: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(Math.round(all * 20 * density / 1.5).toInt(), Math.round(density * 20 / 1.5).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val p2 = Paint()
        p2.style = Paint.Style.FILL
        p2.isAntiAlias = true
        val path: Path
        path = Path()
        path.addCircle(0f, 0f, (density * 5 / 1.5).toFloat(), Path.Direction.CCW)
        canvas.translate((density * 10 / 1.5).toFloat(), (density * 10 / 1.5).toFloat())
        val p3 = Paint()
        p3.isAntiAlias = true
        p3.style = Paint.Style.STROKE
        p3.color = context.resources.getColor(R.color.colorPrimaryText)
        for (i in 0 until all) {
            canvas.translate((density * 20 / 1.5).toFloat(), 0f)
            if (i < fix) {
                p2.color = Color.parseColor("#00ff00")
                p2.alpha = 255
                canvas.drawPath(path, p2)
                canvas.drawPath(path, p3)
            } else {
                p2.color = Color.parseColor("#FF0000")
                p2.alpha = 60
                canvas.drawPath(path, p2)
            }
        }
        return bitmap
    }

    private fun createArrow(@ColorRes colorId: Int): Bitmap {
        val bitmapArrow = Bitmap.createBitmap(Math.round(density * 36), Math.round(density * 36), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapArrow)
        val p2 = Paint()
        p2.style = Paint.Style.FILL
        p2.color = context.resources.getColor(colorId)
        p2.alpha = 180
        val p1 = Paint()
        p1.isAntiAlias = true
        p1.strokeWidth = 2 * density
        p1.style = Paint.Style.STROKE
        p1.color = context.resources.getColor(R.color.colorPrimaryText)
        val path: Path
        path = Path()
        path.reset()
        path.moveTo(Math.round(density * 27 / 1.5).toFloat(), Math.round(density * 5 / 1.5).toFloat())
        path.lineTo(Math.round(density * 45 / 1.5).toFloat(), Math.round(density * 50 / 1.5).toFloat())
        path.lineTo(Math.round(27 * density / 1.5).toFloat(), Math.round(density * 40 / 1.5).toFloat())
        path.lineTo(Math.round(density * 10 / 1.5).toFloat(), Math.round(density * 50 / 1.5).toFloat())
        path.close()
        canvas.drawPath(path, p2)
        canvas.drawPath(path, p1)
        return bitmapArrow
    }

    private fun createBtnArrow(@ColorRes colorId: Int, width: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(Math.round(density * 36), Math.round(density * 36), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val p = Paint()
        p.color = context.resources.getColor(R.color.colorPrimary)
        p.alpha = 180
        val path: Path
        path = Path()
        path.reset()
        path.addCircle((density * 27 / 1.5).toFloat(), (density * 27 / 1.5).toFloat(), (density * 27 / 1.5).toFloat() - width, Path.Direction.CCW)
        path.close()
        p.style = Paint.Style.FILL
        p.isAntiAlias = true
        canvas.drawPath(path, p)
        path.reset()
        path.addCircle((density * 27 / 1.5).toFloat(), (density * 27 / 1.5).toFloat(), (density * 27 / 1.5).toFloat() - width, Path.Direction.CCW)
        path.close()
        p.style = Paint.Style.STROKE
        p.strokeWidth = (width * density / 1.5).toFloat()
        p.isAntiAlias = true
        p.color = context.resources.getColor(R.color.colorPrimaryText)
        canvas.drawPath(path, p)
        canvas.drawBitmap(bitmap, Matrix(), null)
        val scaleArrow = Bitmap.createScaledBitmap(createArrow(colorId), Math.round(40 * density / 1.5).toInt(), Math.round(40 * density / 1.5).toInt(), true)
        canvas.drawBitmap(scaleArrow, (7 * density / 1.5).toFloat(), (6 * density / 1.5).toFloat(), null)
        return bitmap
    }

    private fun createBtnCompass(): Bitmap {
        val bitmap = Bitmap.createBitmap(54, 54, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val path: Path
        path = Path()
        path.reset()
        path.addCircle(27f, 27f, 25f, Path.Direction.CCW)
        path.close()
        var p = Paint()
        p.style = Paint.Style.FILL
        p.isAntiAlias = true
        p.color = context.resources.getColor(R.color.colorPrimary)
        p.alpha = 180
        canvas.drawPath(path, p)
        p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = context.resources.getColor(R.color.colorPrimaryText)
        canvas.drawPath(path, p)
        p = Paint()
        p.style = Paint.Style.FILL
        p.isAntiAlias = true
        p.color = Color.RED
        path.reset()
        path.moveTo(27f, 5f)
        path.lineTo(32f, 27f)
        path.lineTo(22f, 27f)
        path.close()
        canvas.drawPath(path, p)
        p = Paint()
        p.style = Paint.Style.FILL
        p.isAntiAlias = true
        p.color = context.resources.getColor(R.color.colorPrimaryDark)
        path.reset()
        path.moveTo(27f, 49f)
        path.lineTo(32f, 27f)
        path.lineTo(22f, 27f)
        path.close()
        canvas.drawPath(path, p)
        return bitmap
    }

    private fun createBtnZoom(z: String): Bitmap {
        val bitmap = Bitmap.createBitmap(54, 54, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val path: Path
        path = Path()
        path.reset()
        path.addCircle(27f, 27f, 25f, Path.Direction.CCW)
        path.close()
        var p = Paint()
        p.style = Paint.Style.FILL
        p.isAntiAlias = true
        p.color = context.resources.getColor(R.color.colorPrimary)
        p.alpha = 180
        canvas.drawPath(path, p)
        p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = context.resources.getColor(R.color.colorPrimaryText)
        canvas.drawPath(path, p)
        p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.textSize = 50f
        p.style = Paint.Style.FILL
        p.color = context.resources.getColor(R.color.colorPrimaryText)
        val width = p.measureText(z)
        val bounds = Rect()
        p.getTextBounds(z, 0, 1, bounds)
        val height = bounds.height()
        //canvas.translate((54 - width)/2, height+(54-height)/2);
        canvas.translate((54 - width) / 2, 43f)
        canvas.drawText(z, 0f, 0f, p)
        return bitmap
    }


    private fun createBtnWind(): Bitmap {
        val bitmap = Bitmap.createBitmap(54, 54, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val path: Path
        path = Path()
        path.reset()
        path.addCircle(27f, 27f, 25f, Path.Direction.CCW)
        path.close()
        var p = Paint()
        p.style = Paint.Style.FILL
        p.isAntiAlias = true
        p.color = ContextCompat.getColor(context, R.color.colorPrimary)
        p.alpha = 180
        canvas.drawPath(path, p)
        p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        canvas.drawPath(path, p)
        val icon = BitmapFactory.decodeResource(context.resources,
                R.drawable.ic_wind)
        canvas.translate(9f, 8f)
        canvas.scale(0.5f, 0.5f)
        canvas.drawBitmap(icon, Matrix(), null)
        return bitmap
    }

    fun getNextBikeIcon(@DrawableRes resId: Int): Bitmap {
        val icon = BitmapFactory.decodeResource(context.resources, resId)
        val bitmap = Bitmap.createBitmap(Math.round(icon.width / density), Math.round(icon.height / density), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scaleArrow = Bitmap.createScaledBitmap(icon, Math.round(32 * density), Math.round(32 * density), true)
        canvas.drawBitmap(scaleArrow, 0f, 0f, null)
        return scaleArrow
    }

    fun getMarkerPoint(size: Int): Bitmap {
        return createMarkerPoint(size, 0, 1f)
    }

    fun getMarkerPoint(size: Int, @ColorRes colorId: Int): Bitmap {
        return createMarkerPoint(size, colorId, 1f)
    }

    fun getMarkerPoint(size: Int, @ColorRes colorId: Int, strokeWidth: Float): Bitmap {
        return createMarkerPoint(size, colorId, strokeWidth)
    }

    private fun createMarkerPoint(size: Int, @ColorRes colorId: Int, strokeWidth: Float): Bitmap {
        var colorId = colorId
        if (colorId == 0) {
            colorId = R.color.colorPrimary
        }
        val r = size.toFloat() * density / 2
        val bitmap = Bitmap.createBitmap((r * 2).toInt(), (r * 2).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val path: Path
        path = Path()
        path.reset()
        path.addCircle(r, r, r - strokeWidth, Path.Direction.CCW)
        path.close()
        var p = Paint()
        p.style = Paint.Style.FILL
        p.isAntiAlias = true
        p.color = context.resources.getColor(colorId)
        p.alpha = 180
        canvas.drawPath(path, p)
        p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.color = context.resources.getColor(R.color.colorPrimaryText)
        p.strokeWidth = strokeWidth
        canvas.drawPath(path, p)
        return bitmap
    }

    fun getWindDevice(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val path = Path()
        path.reset()
        path.moveTo(width.toFloat()/2,  height.toFloat()/2)
        path.lineTo(0f, 0f)
        path.moveTo(width.toFloat()/2,  height.toFloat()/2)
        path.lineTo(width.toFloat()/2, 0f)


        val r = width* 0.8/2
        for (i in 0..360  step 5) {
            path.moveTo(width.toFloat()/2,  height.toFloat()/2)
            val x = width/2 + r* sin(i*Math.PI/180)
            val y = width/2 - r* cos(i*Math.PI/180)
            path.lineTo(x.toFloat(), y.toFloat())
            // sin a = x/width
        }
        //canvas.drawTextOnPath()

        var p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.alpha = 180
        p.color = ContextCompat.getColor(context, R.color.colorPrimaryText)
        p.strokeWidth = density * 2
        canvas.drawPath(path, p)
        path.reset()
        path.addCircle(width.toFloat()/2,  height.toFloat()/2, height.toFloat()/3, Path.Direction.CCW)
        path.close()
        p.style = Paint.Style.FILL
        p.color = Color.RED
        p.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        canvas.drawPath(path, p)
        return bitmap
    }

}
