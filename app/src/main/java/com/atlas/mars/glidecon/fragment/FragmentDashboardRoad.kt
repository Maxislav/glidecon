package com.atlas.mars.glidecon.fragment

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
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.databinding.FragmentDashboardRoadBinding
import com.atlas.mars.glidecon.dialog.DialogStartAltitude
import com.atlas.mars.glidecon.model.DashboardAltitudeDrawer
import com.atlas.mars.glidecon.model.DashboardVarioDrawer
import com.atlas.mars.glidecon.model.ImageBikeComputer
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.util.LocationUtil
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.AsyncSubject
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class FragmentDashboardRoad : Fragment() {
    lateinit var binding: FragmentDashboardRoadBinding
    lateinit var imageBikeComputer: ImageBikeComputer
    var speedCeloe = ObservableField<String>()
    var speedDrobnoe = ObservableField<String>()
    var varioField = ObservableField<String>()
    var altitudeField = ObservableField<String>()
    var ratiooField = ObservableField<String>()
    private val _onDestroy = AsyncSubject.create<Boolean>();
    private lateinit var varioDrawer: DashboardVarioDrawer
    private lateinit var altitudeDrawer: DashboardAltitudeDrawer

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_PARAM -> {
                    val bundle = msg.data
                    val speed = bundle.getDouble(HANDLER_SPEED_KEY) * 3.6
                    val bitmap = imageBikeComputer.getBitmap(speed)
                    val ratio = bundle.getDouble(HANDLER_RATIO_KEY)
                    setBackground(bitmap, binding.comp)
                    val celoe = speed.toInt()
                    val drobnoe = ((speed - celoe) * 10).toInt()
                    speedCeloe.set(celoe.toString())
                    speedDrobnoe.set(drobnoe.toString())
                    val vario = bundle.getDouble(HANDLER_VARIO_KEY)
                    varioDrawer.setVario(vario.toFloat())
                    setBackground(varioDrawer.bitmap, binding.varioFrame )
                    varioField.set(vario.let {
                        if (0 < it) {
                            DecimalFormat("#.#").format(vario).let { "+$it" }
                        } else {
                            DecimalFormat("#.#").format(vario)
                        }
                    })
                    ratiooField.set(DecimalFormat("#.#").format(ratio))


                }
                WHAT_ALT -> {
                    val bundle = msg.data
                    val altitude = bundle.getDouble(HANDLER_ALT_KEY)
                    setBackground(altitudeDrawer.setAlt(altitude.toFloat()), binding.altFrame)
                    altitudeField.set(DecimalFormat("#").format(altitude))
                }
            }
        }
    }
    companion object{
        private const val HANDLER_SPEED_KEY = "speed"
        private const val HANDLER_RATIO_KEY = "ratio"
        private const val HANDLER_VARIO_KEY = "vario"
        private const val HANDLER_ALT_KEY = "altitude"
        private const val WHAT_PARAM = 1
        private const val WHAT_ALT = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDashboardRoadBinding.inflate(inflater, container, false)
        binding.fragment = this;
        context?.let {
            imageBikeComputer = ImageBikeComputer(it)
            varioDrawer =  DashboardVarioDrawer(it, 300)
            altitudeDrawer =  DashboardAltitudeDrawer(it, 300)
            sendParams(0.0, 0.0, 0.0)
            sendAltitude(0.0)
        }
        onInit()
        return binding.root
    }

    private fun onInit(){

        val locationList = mutableListOf<Location>()
        val locationUtil = LocationUtil()
        MapBoxStore.locationSubject
                .takeUntil(_onDestroy)
                .throttleWithTimeout(100,  TimeUnit.MILLISECONDS)
                .doOnNext {
                    locationList.add(it)
                    while (3 < locationList.size) {
                        locationList.removeAt(0)
                    }
                }
                .filter { 1 < locationList.size }
                .doOnNext {
                    val calcParams = locationUtil.calcParams(locationList)
                    sendParams(calcParams.speed, calcParams.vario, calcParams.ratio)
                }
                .debounce(10000, TimeUnit.MILLISECONDS)
                .subscribeBy {
                    sendParams(0.0, 0.0, 0.0)
                }
        Observables.combineLatest(MapBoxStore.locationSubject, MapBoxStore.startAltitudeSubject.startWith(0.0))
                .takeUntil(_onDestroy)
                .subscribeBy {
                    sendAltitude(it.first.altitude - it.second)
                }
    }

    private fun sendAltitude(altitude: Double){
        val msg: Message = handler.obtainMessage(WHAT_ALT)
        val bundle = Bundle()
        bundle.putDouble(HANDLER_ALT_KEY, altitude)
        msg.data = bundle
        handler.sendMessageDelayed(msg, 40);
    }


    private fun sendParams(speed: Double, vario: Double, ratio: Double){
        val bundle = Bundle()
        bundle.putDouble(HANDLER_SPEED_KEY, speed)
        bundle.putDouble(HANDLER_VARIO_KEY, vario)
        bundle.putDouble(HANDLER_RATIO_KEY, ratio)
        val msg: Message = handler.obtainMessage(WHAT_PARAM)
        msg.data = bundle
        handler.sendMessage(msg);

    }

    private fun setBackground(bitmap: Bitmap, frameLayout: FrameLayout?) {
        val d: Drawable = BitmapDrawable(resources, bitmap)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            frameLayout?.background = d
        } else {
            frameLayout?.setBackgroundDrawable(d)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        _onDestroy.onComplete()
        super.onDestroy()
    }

}