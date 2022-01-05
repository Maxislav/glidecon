package com.atlas.mars.glidecon.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.view.*
import androidx.databinding.DataBindingUtil
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.DialogSaveTrackBinding
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("SimpleDateFormat")
class DialogSaveTrack(ctx: Context, trackName: String, val cb: (trackName: String) -> Unit) : AlertDialog.Builder(ctx) {
    lateinit var alertDialog: AlertDialog
    var binding: DialogSaveTrackBinding = DataBindingUtil.inflate(LayoutInflater.from(ctx), R.layout.dialog_save_track, null, false)

    init {
        binding.dialogSaveTrack = this
        setView(binding.root);

        binding.routeNameText.text = Editable.Factory.getInstance().newEditable(trackName)

        binding.routeNameText.post {
            binding.routeNameText.selectAll()
        }

        binding.routeNameText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.routeNameText.setSelectAllOnFocus(true)
                binding.routeNameText.selectAll()
            }
        }

    }

    fun onClickSave() {
        cb(binding.routeNameText.text.toString())
        onDestroy()
    }

    fun onClickCancel() {
        onDestroy()
    }

    override fun create(): AlertDialog {
        alertDialog = super.create()
        alertDialog.setOnDismissListener {
            onDestroy()
        }
        // alertDialog.getWindow()?.setGravity(Gravity.TOP);
        setDialogPosition(alertDialog)
        return alertDialog
    }

    fun onDestroy() {
        alertDialog.dismiss()
    }

    private fun setDialogPosition(alertDialog: AlertDialog) {
        val windowDialog: Window? = alertDialog.window

        windowDialog?.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)

        val layoutParams = windowDialog?.attributes
        layoutParams?.y = 100
        layoutParams?.x = 0
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        windowDialog?.attributes = layoutParams
    }
}