package com.atlas.mars.glidecon.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.webkit.WebView
import android.widget.LinearLayout
import com.atlas.mars.glidecon.R
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject

class DialogInfoPermission(val activity: Activity) : AlertDialog.Builder(activity)  {
    lateinit var linearLayout: LinearLayout
    lateinit var alertDialog: AlertDialog
    val onAgreeSubject = BehaviorSubject.create<Boolean>()

    init {
        val inflater = LayoutInflater.from(context)
        linearLayout = inflater.inflate(R.layout.dialog_info_permission, null, false) as LinearLayout
        setView(linearLayout)

        val webView = linearLayout.findViewById<WebView>(R.id.web_view)

        val web: CharSequence = activity.resources.getString(R.string.permission_ifo)

        // val formatted: Phrase? = Phrase.from(web)

        webView.loadData(web.toString(), "text/html; charset=UTF-8", null)

        setNegativeButton("Cancel", null)
        setPositiveButton("Agree", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                onAgreeSubject.onNext(true)
            }
        })
    }

    override fun create(): AlertDialog{
        alertDialog = super.create()
        alertDialog.setOnDismissListener {
            onAgreeSubject.onNext(false)
        }
        return alertDialog
    }
}