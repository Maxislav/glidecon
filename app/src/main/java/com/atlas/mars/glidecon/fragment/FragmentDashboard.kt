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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.FragmentDashboardBinding
import com.atlas.mars.glidecon.model.DashboardAltitudeDrawer
import com.atlas.mars.glidecon.model.DashboardSpeedDrawer
import com.atlas.mars.glidecon.model.DashboardVarioDrawer
import com.atlas.mars.glidecon.model.MyImage
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.util.LocationUtil
import com.atlas.mars.glidecon.view.CustomFontTextView
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import java.text.DecimalFormat


class FragmentDashboard : Fragment() {
    //  lateinit var dashboardDrawer: DashboardSpeedDrawer
    private val TAG = "FragmentDashboard"
    private lateinit var handler: Handler
    var isSubscribed = true;
    private var speedFrame: FrameLayout? = null
    private var varioFrame: FrameLayout? = null
    private var altFrame: FrameLayout? = null

    private var speedView: CustomFontTextView? = null
    private var speedViewFr: CustomFontTextView? = null
    private var varioView: CustomFontTextView? = null
    private var altView: CustomFontTextView? = null

    // private var ratioView: CustomFontTextView? = null
    private val locationList = mutableListOf<Location>()
    private lateinit var speedDrawer: DashboardSpeedDrawer
    private lateinit var varioDrawer: DashboardVarioDrawer
    private lateinit var altDrawer: DashboardAltitudeDrawer
    private lateinit var binding: FragmentDashboardBinding

    companion object {
        const val HANDLER_SPEED_KEY = "speed"
        const val HANDLER_RATIO_KEY = "ratio"
        const val HANDLER_VARIO_KEY = "vario"
        const val HANDLER_ALT_KEY = "altitude"
        const val WHAT_PARAM = 1
        const val WHAT_ALT = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        // return inflater.inflate(R.layout.fragment_dashboard, null)
        return binding.root;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        speedView = view?.findViewById(R.id.speed_view)
        speedViewFr = view?.findViewById(R.id.speed_view_fractional)
        varioView = view?.findViewById(R.id.vario_view)
        altView = view?.findViewById(R.id.alt_view)
        // ratioView = view?.findViewById(R.id.ratio_view)

        speedFrame = view?.findViewById(R.id.speed_frame)
        varioFrame = view?.findViewById(R.id.vario_frame)
        altFrame = view?.findViewById(R.id.alt_frame)
        altFrame?.post {
            altFrame?.let { altimeterCreated(it.width) }
        }

        speedFrame?.post(Runnable {
            speedFrame?.let { speedFrameCreated(it.width) }
        })
        varioFrame?.post(Runnable {
            varioFrame?.let { varioFrameCreated(it.width) }
        })


        /* speedView = view?.findViewById(R.id.speed_view)
         speedViewFr = view?.findViewById(R.id.speed_view_fractional)
         ratioView = view?.findViewById(R.id.ratio_view)*/
        view?.post(Runnable {
            onViewCreated()
        })
    }

    private fun altimeterCreated(size: Int) {
        altDrawer = DashboardAltitudeDrawer(activity as Context, size)
        altDrawer.setAlt(0.0f)
        setBackground(altDrawer.bitmap, altFrame)
    }

    private fun updateAltimeterDrawer(alt: Double) {
        altDrawer.setAlt(alt.toFloat())
        setBackground(altDrawer.bitmap, altFrame)
    }

    private fun varioFrameCreated(size: Int) {
        varioDrawer = DashboardVarioDrawer(activity as Context, size)
        varioDrawer.setVario(0.0f)
        setBackground(varioDrawer.bitmap, varioFrame)
    }

    private fun updateVarioDrawer(vario: Double) {
        varioDrawer.setVario(vario.toFloat())
        setBackground(varioDrawer.bitmap, varioFrame)
    }

    private fun speedFrameCreated(size: Int) {
        speedDrawer = DashboardSpeedDrawer(activity as Context, size)
        speedDrawer.setSpeed(0.0f)
        setBackground(speedDrawer.bitmap, speedFrame)
    }

    private fun updateSpeedDrawer(speed: Double) {
        speedDrawer.setSpeed(speed.toFloat())
        setBackground(speedDrawer.bitmap, speedFrame)
    }

    private fun setBackground(bitmap: Bitmap, frameLayout: FrameLayout?) {
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

                when (msg.what) {
                    WHAT_PARAM -> {
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
                            //ratioView?.text = DecimalFormat("#.#").format(ratio)
                            binding.myRatio = DecimalFormat("#.#").format(ratio)
                        }

                        varioView?.text = vario.let {
                            if (0 < it) {
                                DecimalFormat("#.#").format(vario).let { "+$it" }
                            } else {
                                DecimalFormat("#.#").format(vario)
                            }
                        }
                    }
                    WHAT_ALT -> {
                        val bundle = msg.data
                        val altitude = bundle.getDouble(HANDLER_ALT_KEY)
                        updateAltimeterDrawer(altitude)
                        altView?.text = DecimalFormat("#").format(altitude)
                    }
                }
            }
        }



        val locationUtil = LocationUtil()
        MapBoxStore.locationSubject
                .takeWhile { isSubscribed }
                .doOnNext {
                    locationList.add(it)
                    while (3 < locationList.size) {
                        locationList.removeAt(0)
                    }
                }
                .filter { 1 < locationList.size }
                .subscribeBy {
                    val bundle = Bundle()
                    val msg: Message = handler.obtainMessage(WHAT_PARAM)
                    val calcParams = locationUtil.calcParams(locationList)
                    bundle.putDouble(HANDLER_SPEED_KEY, calcParams.speed)
                    bundle.putDouble(HANDLER_VARIO_KEY, calcParams.vario)
                    bundle.putDouble(HANDLER_RATIO_KEY, calcParams.k)
                    msg.data = bundle
                    handler.sendMessage(msg);
                }


        Observables.combineLatest(MapBoxStore.locationSubject, MapBoxStore.startAltitudeSubject)
                .takeWhile { isSubscribed }
                .subscribeBy {
                    val msg: Message = handler.obtainMessage(WHAT_ALT)
                    val bundle = Bundle()
                    bundle.putDouble(HANDLER_ALT_KEY, it.first.altitude - it.second)
                    msg.data = bundle
                    handler.sendMessage(msg);
                }

    }



    override fun onDestroy() {
        super.onDestroy()
        isSubscribed = false
        handler.removeCallbacksAndMessages(null);
    }
}