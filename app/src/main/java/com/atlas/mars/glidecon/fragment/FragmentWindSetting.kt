package com.atlas.mars.glidecon.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.model.MyImage
import kotlinx.android.synthetic.main.nav_header_main.*


class FragmentWindSetting : Fragment() {
    val TAG = "FragmentWindSetting"

    private val screenWidth: Int
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity!!.windowManager.currentWindowMetrics
                val insets: Insets = windowMetrics.windowInsets
                        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                windowMetrics.bounds.width() - insets.left - insets.right
            } else {
                val displayMetrics = DisplayMetrics()
                activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
                displayMetrics.widthPixels
            }
        }
    private val screenHeight: Int
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity!!.windowManager.currentWindowMetrics
                val insets: Insets = windowMetrics.windowInsets
                        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                windowMetrics.bounds.height() - insets.top - insets.bottom
            } else {
                val displayMetrics = DisplayMetrics()
                activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
                displayMetrics.heightPixels
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wing_setting, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = activity as Context

        val myImage = MyImage(context)

        val imageView: ImageView? = view?.findViewById<ImageView>(R.id.wind_image_view)
        val size = Math.min(screenWidth, screenHeight)

        imageView?.layoutParams?.height = size;
        imageView?.layoutParams?.width = size;
        //val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        Log.d(TAG, "screenSize = $screenHeight $screenHeight")
        imageView?.setImageBitmap(myImage.getWindDevice(size, size))

        imageView?.setOnTouchListener(object : View.OnTouchListener {

            val g = 0
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                return true
            }

        })


    }


}