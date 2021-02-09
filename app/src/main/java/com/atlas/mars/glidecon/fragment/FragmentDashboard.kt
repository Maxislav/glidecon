package com.atlas.mars.glidecon.fragment

import android.annotation.SuppressLint
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

    companion object {
        const val HANDLER_KEY = "speed"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_dashboard, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        speedView = view?.findViewById(R.id.speed_view)
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
            @SuppressLint("SetTextI18n")
            override fun handleMessage(msg: Message) {
                //  val location: Location msg.
                val bundle = msg.data
                val speed = bundle.getDouble(HANDLER_KEY) * 3.6
                val speedFloat = DecimalFormat("#.#").format(speed).toFloat()
                dashboardDrawer.draw(speedFloat)
                setBackground()
                speedView?.text = speedFloat.toString()
            }
        }

        val locationList = mutableListOf<Location>()

        MapBoxStore.locationSubject
                .takeWhile { isSubscribed }
                .subscribeBy {
                    locationList.add(it)

                    if (1 < locationList.size) {
                        while (3000 < (locationList.last().time - locationList.first().time)) {
                            locationList.removeAt(0)
                        }
                        val speedList = mutableListOf<Double>()
                        for (i in 0..locationList.size - 2) {
                            val previousLocation = locationList[i]
                            val currentLocation = locationList[i + 1]
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
                            bundle.putDouble(HANDLER_KEY, speed)
                            msg.setData(bundle)
                            handler.sendMessage(msg);
                        }

                    }

                }

    }

    private fun createBitmap(size: Int) {
        dashboardDrawer = DashboardDrawer(activity as Context, size)
        dashboardDrawer.draw(0.0f)
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

    override fun onDestroy() {
        super.onDestroy()
        isSubscribed = false
    }
}