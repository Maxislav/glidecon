package com.atlas.mars.glidecon.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.model.DashboardSpeedDrawer
import com.atlas.mars.glidecon.model.DashboardVarioDrawer
import com.atlas.mars.glidecon.model.MyImage
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.view.CustomFontTextView
import io.reactivex.rxkotlin.subscribeBy
import java.text.DecimalFormat


class FragmentDashboard : Fragment() {
   //  lateinit var dashboardDrawer: DashboardSpeedDrawer
    private lateinit var handler: Handler
    var isSubscribed = true;
    private var speedFrame: FrameLayout? = null
    private var varioFrame: FrameLayout? = null
    private var altFrame: FrameLayout? = null

    private var speedView: CustomFontTextView? = null
    private var speedViewFr: CustomFontTextView? = null
    private var ratioView: CustomFontTextView? = null
    private var varioView: CustomFontTextView? = null
    private val locationList = mutableListOf<Location>()
    private lateinit var speedDrawer: DashboardSpeedDrawer
    private lateinit var varioDrawer: DashboardVarioDrawer

    companion object {
        const val HANDLER_SPEED_KEY = "speed"
        const val HANDLER_RATIO_KEY = "ratio"
        const val HANDLER_VARIO_KEY = "vario"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_dashboard, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        speedView = view?.findViewById(R.id.speed_view)
        speedViewFr = view?.findViewById(R.id.speed_view_fractional)
        varioView = view?.findViewById(R.id.vario_view)

        speedFrame = view?.findViewById(R.id.speed_frame)
        varioFrame = view?.findViewById(R.id.vario_frame)
        altFrame = view?.findViewById(R.id.alt_frame)
        speedFrame?.post(Runnable {
            speedFrame?.let { speedFrameCreated(it.width) }
        })
        varioFrame?.post(Runnable {
            varioFrame?.let{varioFrameCreated(it.width)}
        })


        /* speedView = view?.findViewById(R.id.speed_view)
         speedViewFr = view?.findViewById(R.id.speed_view_fractional)
         ratioView = view?.findViewById(R.id.ratio_view)*/
        view?.post(Runnable {
            onViewCreated()
        })
    }

    private fun varioFrameCreated(size: Int){
        varioDrawer = DashboardVarioDrawer(activity as Context, size)
        varioDrawer.setVario(0.0f)
        setBackground(varioDrawer.bitmap, varioFrame)
    }

    private fun updateVarioDrawer(vario: Double){
        varioDrawer.setVario(vario.toFloat())
        setBackground(varioDrawer.bitmap, varioFrame)
    }

    private fun speedFrameCreated(size: Int) {
        speedDrawer = DashboardSpeedDrawer(activity as Context, size)
        speedDrawer.setSpeed(0.0f)
        setBackground(speedDrawer.bitmap, speedFrame)
    }

    private fun updateSpeedDrawer(speed: Double){
        speedDrawer.setSpeed(speed.toFloat())
        setBackground(speedDrawer.bitmap, speedFrame)
    }

    private fun setBackground(bitmap: Bitmap, frameLayout: FrameLayout?){
        val d: Drawable = BitmapDrawable(resources, bitmap)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            frameLayout?.background = d
        } else {
            frameLayout?.setBackgroundDrawable(d)
        }
    }

    private fun onViewCreated() {

        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                //  val location: Location msg.
                val bundle = msg.data
                val ratio = bundle.getDouble(HANDLER_RATIO_KEY)
                val speed = bundle.getDouble(HANDLER_SPEED_KEY) * 3.6
                val vario = bundle.getDouble(HANDLER_VARIO_KEY)

                updateSpeedDrawer(speed)
                updateVarioDrawer(vario)
                val celoe = speed.toInt()
                val drobnoe = ((speed - celoe) * 10).toInt()

                speedView?.text = celoe.toString()
                speedViewFr?.text = drobnoe.toString()
                if (ratio < 2000) {
                    ratioView?.text = DecimalFormat("#.#").format(ratio)
                }
                varioView?.text = vario?.let {
                    if(0<it){
                        DecimalFormat("#.#").format(vario).let{"+$it"}
                    }else{
                        DecimalFormat("#.#").format(vario)
                    }
                }

            }
        }

        val locationListSpeed = mutableListOf<Location>()
        val locationListVario = mutableListOf<Location>()

        MapBoxStore.locationSubject
                .takeWhile { isSubscribed }
                .doOnNext {
                    locationList.add(it)
                    locationListVario.add(it)
                }
                .subscribeBy {
                    locationListSpeed.add(it)

                    if (1 < locationListSpeed.size) {
                        val k = calcDragRatio()

                        while (3000 < (locationListSpeed.last().time - locationListSpeed.first().time)) {
                            locationListSpeed.removeAt(0)
                        }
                        val speedList = mutableListOf<Double>()
                        for (i in 0..locationListSpeed.size - 2) {
                            val previousLocation = locationListSpeed[i]
                            val currentLocation = locationListSpeed[i + 1]
                            val dTime = (currentLocation.time - previousLocation.time) / 1000
                            val distance = previousLocation.distanceTo(currentLocation)
                            val speed = distance / dTime
                            speedList.add(speed.toDouble())
                        }
                        /** vario **/
                        while (3<locationListVario.size){
                            locationListVario.removeAt(0)
                        }
                        val varioList = mutableListOf<Double>()

                        for (i in 0..locationListVario.size - 2){
                            val previousLocation = locationListVario[i]
                            val currentLocation = locationListVario[i + 1]
                            val dTime = (currentLocation.time - previousLocation.time) / 1000
                            val dAlt = currentLocation.altitude - previousLocation.altitude
                            varioList.add(dAlt/dTime)
                        }
                        var varioSum = 0.0
                        for(vario in varioList){
                            varioSum+=vario
                        }
                        val vario = varioSum/varioList.size



                        var sum = 0.0
                        for (speed in speedList) {
                            sum += speed
                        }
                        val speed = sum / speedList.size
                        if (speed < 600) {
                            val msg: Message = handler.obtainMessage()
                            val bundle = Bundle()
                            bundle.putDouble(HANDLER_SPEED_KEY, speed)
                            bundle.putDouble(HANDLER_RATIO_KEY, k)
                            bundle.putDouble(HANDLER_VARIO_KEY, vario)
                            msg.data = bundle
                            handler.sendMessage(msg);
                        }

                    }

                }

    }


    private fun calcDragRatio(): Double {
        val kList = mutableListOf<Double>()
        for (i in 0..locationList.size - 2) {
            val previousLocation = locationList[i]
            val currentLocation = locationList[i + 1]
            val distance = previousLocation.distanceTo(currentLocation) // m
            val dTime = currentLocation.time - previousLocation.time
            val dAltitude = previousLocation.altitude - currentLocation.altitude
            if (0 < distance && dAltitude != 0.0) {
                kList.add(distance / dAltitude)
            }

        }
        var kSum = 0.0
        if (0 < kList.size) {
            for (k in kList) {
                kSum += k
            }
            return kSum / kList.size
        }
        return 0.0

    }

    override fun onDestroy() {
        super.onDestroy()
        isSubscribed = false
        handler.removeCallbacksAndMessages(null);
    }
}