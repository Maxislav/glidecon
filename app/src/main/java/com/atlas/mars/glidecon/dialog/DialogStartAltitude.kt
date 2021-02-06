package com.atlas.mars.glidecon.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.liftToDragRatioSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.locationSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.optimalSpeedSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.startAltitudeSubject
import io.reactivex.rxkotlin.subscribeBy
import java.lang.Exception
import java.text.DecimalFormat

class DialogStartAltitude(val activity: Activity) : AlertDialog.Builder(activity) {
    lateinit var linearLayout: LinearLayout
    lateinit var alertDialog: AlertDialog
    var isSubscribe = true
    var handler: Handler
    var location: Location? = null

    init {
        val inflater = LayoutInflater.from(context)
        linearLayout = inflater.inflate(R.layout.dialog_start_altitude, null, false) as LinearLayout
        setView(linearLayout)

        val currentAltTextView: TextView = linearLayout.findViewById(R.id.current_altitude)

        val startAltitude: EditText = linearLayout.findViewById(R.id.start_altitude)

        val saveButton: Button = linearLayout.findViewById(R.id.save_button)

        val setCurrentButton: Button = linearLayout.findViewById(R.id.set_current)
        setCurrentButton.isEnabled = false

        val liftToDragText: EditText = linearLayout.findViewById(R.id.lift_to_drag_text)

        val optimalSpeedView: EditText  = linearLayout.findViewById(R.id.optimal_speed_view)

        linearLayout.findViewById<Button>(R.id.cancel_button)
                .setOnClickListener {
                    alertDialog.dismiss()
                }
        handler = object : Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            override fun handleMessage(msg: Message) {

                location = msg.obj as Location
                val altitude = DecimalFormat("#.#").format(location!!.altitude)
                currentAltTextView.text = altitude
                setCurrentButton.isEnabled = true
            }
        }




        setCurrentButton.setOnClickListener {
            location?.let { it1 ->
                startAltitude.setText(DecimalFormat("#.#").format(it1.altitude))
            }
        }
        saveButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var startAltitudeText = startAltitude.text.toString()
                var liftToDragTextText = liftToDragText.text.toString()
                var optimalSpeedViewText = optimalSpeedView.text.toString()
                var textDouble: Double?
                try {
                    textDouble = startAltitudeText.toDouble()
                    startAltitudeText = textDouble.let { DecimalFormat("#.#").format(it) }
                    textDouble = liftToDragTextText.toDouble()
                    liftToDragTextText = textDouble.let { DecimalFormat("#.#").format(it) }

                    textDouble = optimalSpeedViewText.toDouble()
                    optimalSpeedViewText = textDouble.let { DecimalFormat("#.#").format(it) }

                } catch (e: Exception) {
                    alertDialog.dismiss()
                    return
                }


                alertDialog.dismiss()
                optimalSpeedSubject.onNext(optimalSpeedViewText.toDouble())
                liftToDragRatioSubject.onNext(liftToDragTextText.toDouble())
                startAltitudeSubject.onNext(startAltitudeText.toDouble())
            }
        })
        optimalSpeedSubject
                .takeWhile { isSubscribe }
                .subscribeBy {
                    optimalSpeedView.setText(DecimalFormat("#.#").format(it))
                }
        liftToDragRatioSubject
                .takeWhile { isSubscribe }
                .subscribeBy {
                    liftToDragText.setText(DecimalFormat("#.#").format(it))
                }
        startAltitudeSubject
                .takeWhile { isSubscribe }
                .subscribeBy {
                    startAltitude.setText(DecimalFormat("#.#").format(it))
                }
        locationSubject
                .takeWhile { isSubscribe }
                .subscribeBy {
                    val msg = handler.obtainMessage(1, it)
                    handler.sendMessage(msg)
                }
    }

    override fun create(): AlertDialog {
        alertDialog = super.create()
        alertDialog.setOnDismissListener {
            onDestroy()
        }
        return alertDialog
    }

    private fun onDestroy() {
        isSubscribe = false
        handler.removeCallbacksAndMessages(null);
    }
}