package com.atlas.mars.glidecon.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.DialogSaveTrackBinding

enum class DialogSaveTrackAction {
    SAVE,
    CANCEL
}

class DialogSaveTrack(val c: Context, val cb: (action: DialogSaveTrackAction) -> Unit) : AlertDialog.Builder(c) {
    lateinit var alertDialog: AlertDialog
    init {
        val binding: DialogSaveTrackBinding = DataBindingUtil.inflate(LayoutInflater.from(c), R.layout.dialog_save_track, null, false)
        binding.dialogSaveTrack = this
        setView(binding.root);

    }

    fun onClickSave() {
        cb(DialogSaveTrackAction.SAVE)
        onDestroy()
    }

    fun onClickCancel() {
        cb(DialogSaveTrackAction.CANCEL)
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
    fun onDestroy(){
        alertDialog.dismiss()
    }

    private fun setDialogPosition(alertDialog: AlertDialog){
        val windowDialog: Window? = alertDialog.window

        windowDialog?.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)

        val layoutParams = windowDialog?.attributes
        layoutParams?.y = 100
        layoutParams?.x = 0
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        windowDialog?.attributes = layoutParams
    }
}