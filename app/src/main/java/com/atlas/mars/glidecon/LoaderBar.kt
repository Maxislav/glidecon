package com.atlas.mars.glidecon

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout

class LoaderBar(var activity: Activity) {
    //var activity: Activity? = null
    var inflater: LayoutInflater = activity.layoutInflater
    private val  globalLayout = activity.getWindow().getDecorView().getRootView() as FrameLayout?

    var linearLayoutCOntext: FrameLayout? = null
    var progress: LinearLayout? = null
    var density = 0f
    private var count = 0


    fun onCreate(){
        val v  = inflater.inflate(R.layout.progressbar, null, false)
        density = activity.resources.displayMetrics.density

        linearLayoutCOntext = v as FrameLayout
        progress = linearLayoutCOntext!!.findViewById<View>(R.id.progress) as LinearLayout
        v.visibility = View.INVISIBLE
        globalLayout!!.addView(v)
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (3 * density).toInt())
        layoutParams.gravity = Gravity.TOP
        linearLayoutCOntext!!.layoutParams = layoutParams
    }

    fun show() {
        if (linearLayoutCOntext == null) {
            onCreate()
        }
        linearLayoutCOntext!!.visibility = View.VISIBLE
        count++
        val animIn = AnimationUtils.loadAnimation(activity.applicationContext, R.anim.sender)
        animIn.repeatCount = Animation.INFINITE
        progress!!.startAnimation(animIn)
    }

    fun isWork(): Boolean {
        return 0 < count
    }


    fun hide(): LoaderBar? {
        if (0 < count) {
            count--
        }
        if (count <= 0) {
            stopAll()
        }
        return this
    }

    fun stopAll() {
        count = 0
        progress!!.clearAnimation()
        globalLayout!!.removeView(linearLayoutCOntext)
        linearLayoutCOntext = null
    }
}