package com.atlas.mars.glidecon.fragment

import android.content.Context
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
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.model.DashboardDrawer
import com.atlas.mars.glidecon.model.MyImage
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.view.CustomFontTextView
import io.reactivex.rxkotlin.subscribeBy
import java.text.DecimalFormat


class FragmentDashboard : Fragment() {
    lateinit var dashboardDrawer: DashboardDrawer
    private lateinit var handler: Handler
    var isSubscribed = true;
    private var speedView: CustomFontTextView? = null
    private var speedViewFr: CustomFontTextView? = null
    private var ratioView: CustomFontTextView? = null
    private val locationList = mutableListOf<Location>()

    companion object {
        const val HANDLER_SPEED_KEY = "speed"
        const val HANDLER_RATIO_KEY = "ratio"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_dashboard, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        speedView = view?.findViewById(R.id.speed_view)
        speedViewFr = view?.findViewById(R.id.speed_view_fractional)
        ratioView = view?.findViewById(R.id.ratio_view)
        view?.post(Runnable {
            onViewCreated()
        })
    }

    private fun onViewCreated() {
        val myImage = MyImage(activity as Context)
        myImage.arrow

        val width = view?.width
        width?.let { createBitmap(it) }
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                //  val location: Location msg.
                val bundle = msg.data
                val ratio = bundle.getDouble(HANDLER_RATIO_KEY)
                val speed = bundle.getDouble(HANDLER_SPEED_KEY) * 3.6
                val speedFloat = DecimalFormat("#.#").format(speed).toFloat()
                dashboardDrawer.setSpeed(speedFloat)
                setBackground()
                val celoe = speed.toInt()
                val drobnoe = ((speed - celoe) * 10).toInt()

                speedView?.text = celoe.toString()
                speedViewFr?.text = drobnoe.toString()
                if(ratio<2000){
                    ratioView?.text = DecimalFormat("#.#").format(ratio)
                }

            }
        }

        val locationListSpeed = mutableListOf<Location>()

        MapBoxStore.locationSubject
                .takeWhile { isSubscribed }
                .doOnNext{
                    locationList.add(it)
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
                            msg.data = bundle
                            handler.sendMessage(msg);
                        }

                    }

                }

    }

    private fun createBitmap(size: Int) {
        dashboardDrawer = DashboardDrawer(activity as Context, size)
        dashboardDrawer.setSpeed(0.0f)
        setBackground()
    }

    fun setBackground() {
        val d: Drawable = BitmapDrawable(resources, dashboardDrawer.bitmap)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view?.background = d
        } else {
            view?.setBackgroundDrawable(d)
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