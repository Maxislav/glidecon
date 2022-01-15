package com.atlas.mars.glidecon.model

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.atlas.mars.glidecon.ListSavedTrack
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.TtoastBinding
import kotlinx.coroutines.*


class TToast(activity: Activity) {
    private val density: Float = Density(activity).density
    private var binding: TtoastBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.ttoast, null, false)
    private val globalLayout: FrameLayout = activity.window.decorView.rootView as FrameLayout


    fun show(msg: String, timeout: Long = 2000, cb: (tToast: TToast) -> Unit) {
        binding.myText = msg
        // val v = activity.window.decorView.rootView as FrameLayout
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.TOP or Gravity.CENTER
        layoutParams.setMargins(0, (density * 50).toInt(), 0, 0)
        binding.root.layoutParams = layoutParams
        globalLayout.addView(binding.root)
        var handler: Handler? = null
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                globalLayout.removeViewInLayout(binding.root)
                cb(this@TToast)
                handler?.removeCallbacksAndMessages(null)
            }
        }
        handler.sendEmptyMessageDelayed(1, timeout)
    }

}