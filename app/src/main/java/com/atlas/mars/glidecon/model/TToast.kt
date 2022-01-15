package com.atlas.mars.glidecon.model
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.TtoastBinding
import kotlinx.coroutines.*

class TToast(val activity: Activity) {
    private val density: Float = Density(activity).density
    private var binding: TtoastBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.ttoast, null, false)
    private val globalLayout: FrameLayout = activity.window.decorView.rootView as FrameLayout

    var message = ""
    var type = Type.DEFAULT
    var background = ContextCompat.getDrawable(activity, R.drawable.corner)
    var textColor = ContextCompat.getColor(activity, R.color.colorPrimaryText)

    init {
        binding.tToast = this
    }
    fun show(msg: String) {
        show(msg, Type.DEFAULT, 2000) {}
    }

    fun show(msg: String, type: Type, timeout: Long = 2000) {
        show(msg, type, timeout) {}
    }

    fun show(msg: String, type: Type,  timeout: Long, cb: (tToast: TToast) -> Unit) {
        when (type) {
            Type.ERROR -> {
                background = ContextCompat.getDrawable(activity, R.drawable.corner_red)
                textColor = ContextCompat.getColor(activity, R.color.white)
            }
            Type.DEFAULT -> {
                background = ContextCompat.getDrawable(activity, R.drawable.corner)
                textColor = ContextCompat.getColor(activity, R.color.colorPrimaryText)
            }
            else -> {
                background = ContextCompat.getDrawable(activity, R.drawable.corner)
                textColor = ContextCompat.getColor(activity, R.color.colorPrimaryText)
            }
        }
        message = msg
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.TOP or Gravity.CENTER
        layoutParams.setMargins(0, (density * 150).toInt(), 0, 0)
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

    enum class Type {
        WARNING,
        ERROR,
        DEFAULT,
    }
}