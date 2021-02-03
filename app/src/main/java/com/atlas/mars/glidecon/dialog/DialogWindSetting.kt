package com.atlas.mars.glidecon.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.model.MyImage

class DialogWindSetting(val activity: Activity) : AlertDialog.Builder(activity) {
    private val TAG = "DialogWindSetting"
    lateinit var alertDialog: AlertDialog
    lateinit var linearLayout: RelativeLayout
    private val screenWidth: Int
        get() {
            val displayRectangle = Rect()
            dialogWindow?.decorView?.getWindowVisibleDisplayFrame(displayRectangle);
            return displayRectangle.width()
        }

    private val screenHeight: Int
        get() {
            val displayRectangle = Rect()
            dialogWindow?.decorView?.getWindowVisibleDisplayFrame(displayRectangle);
            return displayRectangle.height()
        }

    private val dialogWindow: Window?
        get() {
            return alertDialog.window
        }

    init {
        val inflater = LayoutInflater.from(context)
        linearLayout = inflater.inflate(R.layout.dialog_wind_setting, null, false) as RelativeLayout
        setView(linearLayout)
        setPositiveButton("ok", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                Log.d(TAG, "ok")
            }
        })
        setNegativeButton("cancel", null)
    }

    @SuppressLint("ResourceAsColor")
    override fun create(): AlertDialog {
        alertDialog = super.create()


        val imageView: ImageView = linearLayout.findViewById(R.id.wind_image_view)
        val size = Math.min(screenWidth, screenHeight)
        imageView.layoutParams.height = size
        imageView.layoutParams.width = size
        alertDialog.setOnShowListener(DialogInterface.OnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.colorPrimaryText);
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.colorPrimaryText);
            val myImage = MyImage(activity as Context)
            val size1 = Math.min(linearLayout.width, linearLayout.height)
            imageView.layoutParams.height = size1
            imageView.layoutParams.width = size1
            imageView.setImageBitmap(myImage.getWindDevice(size1, size1))
        })

        val windowDialog: Window? = alertDialog.getWindow()

        windowDialog?.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)

        val layoutParams = windowDialog?.attributes
        layoutParams?.y = 100
        layoutParams?.x = 0
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        windowDialog?.attributes = layoutParams
        return alertDialog
    }

    fun getWindowSize() {

    }
}