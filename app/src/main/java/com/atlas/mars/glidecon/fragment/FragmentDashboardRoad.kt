package com.atlas.mars.glidecon.fragment

import android.annotation.SuppressLint
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
import com.atlas.mars.glidecon.database.MapDateBase
import com.atlas.mars.glidecon.databinding.FragmentDashboardRoadBinding
import com.atlas.mars.glidecon.dialog.DialogStartAltitude
import com.atlas.mars.glidecon.model.DashboardAltitudeDrawer
import com.atlas.mars.glidecon.model.DashboardVarioDrawer
import com.atlas.mars.glidecon.model.ImageBikeComputer
import com.atlas.mars.glidecon.model.RoutePoints
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.util.LocationUtil
import com.mapbox.mapboxsdk.geometry.LatLng
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class FragmentDashboardRoad : Fragment() {
    lateinit var binding: FragmentDashboardRoadBinding
    lateinit var imageBikeComputer: ImageBikeComputer
    var speedCeloe = ObservableField<String>()
    var speedDrobnoe = ObservableField<String>()
    var varioField = ObservableField<String>()
    var altitudeField = ObservableField<String>()
    var ratioField = ObservableField<String>()
    val distanceField = ObservableField<String>()
    private val _onDestroy = AsyncSubject.create<Boolean>();
    private lateinit var varioDrawer: DashboardVarioDrawer
    private lateinit var altitudeDrawer: DashboardAltitudeDrawer
    private lateinit var mapDateBase: MapDateBase
    var locationDisposable: Disposable = object : Disposable {
        override fun dispose() {
        }

        override fun isDisposed(): Boolean {
            return false
        }

    }

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_PARAM -> {
                    val bundle = msg.data
                    val speed = bundle.getDouble(HANDLER_SPEED_KEY) * 3.6
                    val bitmap = imageBikeComputer.getBitmap(speed)
                    val ratio: Double = bundle.getDouble(HANDLER_RATIO_KEY)
                    setBackground(bitmap, binding.comp)
                    val celoe = speed.toInt()
                    val drobnoe = ((speed - celoe) * 10).toInt()
                    speedCeloe.set(celoe.toString())
                    speedDrobnoe.set(drobnoe.toString())
                    val vario = bundle.getDouble(HANDLER_VARIO_KEY)
                    varioDrawer.setVario(vario.toFloat())
                    setBackground(varioDrawer.bitmap, binding.varioFrame)
                    varioField.set(vario.let {
                        if (0 < it) {
                            DecimalFormat("#.#").format(vario).let { "+$it" }
                        } else {
                            DecimalFormat("#.#").format(vario)
                        }
                    })
                    when {
                        50 < abs(ratio) -> {
                            ratioField.set("--")
                        }
                        10 < abs(ratio) -> {
                            ratioField.set(floor(ratio).toString())
                        }
                        else -> {
                            ratioField.set(DecimalFormat("#.#").format(ratio))
                        }
                    }
                }
                WHAT_ALT -> {
                    val bundle = msg.data
                    val altitude = bundle.getDouble(HANDLER_ALT_KEY)
                    setBackground(altitudeDrawer.setAlt(altitude.toFloat()), binding.altFrame)
                    altitudeField.set(DecimalFormat("#").format(altitude))
                }
                WHAT_DIST -> {
                    val bundle = msg.data
                    val distFloat = bundle.getFloat(HANDLER_DIST_KEY)
                    distanceField.set(DecimalFormat("#.#").format(distFloat))
                }
            }
        }
    }

    companion object {
        private const val HANDLER_SPEED_KEY = "speed"
        private const val HANDLER_RATIO_KEY = "ratio"
        private const val HANDLER_VARIO_KEY = "vario"
        private const val HANDLER_ALT_KEY = "altitude"
        private const val HANDLER_DIST_KEY = "distance"
        private const val WHAT_PARAM = 1
        private const val WHAT_ALT = 2
        private const val WHAT_DIST = 3
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDashboardRoadBinding.inflate(inflater, container, false)
        binding.fragment = this;
        context?.let {
            mapDateBase = MapDateBase(it)
            imageBikeComputer = ImageBikeComputer(it)
            varioDrawer = DashboardVarioDrawer(it, 300)
            altitudeDrawer = DashboardAltitudeDrawer(it, 300)
            sendParams(0.0, 0.0, 0.0)
            sendAltitude(0.0)
        }
        onInit()
        return binding.root
    }

    @SuppressLint("CheckResult")
    private fun onInit() {

        val locationList = mutableListOf<Location>()
        val locationUtil = LocationUtil()
        MapBoxStore.locationSubject
                .takeUntil(_onDestroy)
                .throttleWithTimeout(100, TimeUnit.MILLISECONDS)
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
        MapBoxStore.activeRoute
                .takeUntil(_onDestroy)
                .subscribeOn(Schedulers.newThread())
                .subscribe {
                    if (-1 < it.toInt()) {
                        val routePoints = mapDateBase.getRoutePoints(it)
                        startCalculationDist(routePoints)
                    } else {
                        if (!locationDisposable.isDisposed) {
                            locationDisposable.dispose()
                        }
                    }
                }
    }


    @DelicateCoroutinesApi
    private fun startCalculationDist(routePoints: MutableList<RoutePoints>) {
        if (!locationDisposable.isDisposed) {
            locationDisposable.dispose()
        }

        GlobalScope.launch(Dispatchers.IO) {
            val locationList = routePoints.filter { it.type === MapBoxStore.PointType.ROUTE }.map { getLocation(it) }
            val filledLocationList = fillLocationList(locationList)
            locationListener(filledLocationList)
        }

    }

    private fun fillLocationList(locationList: List<Location>): List<Location> {
        val newLocationList = mutableListOf<Location>();

        for (i in 0..(locationList.size - 2)) {
            val distance = locationList[i].distanceTo(locationList[i + 1])
            if (1000 < distance) {
                val k = ceil((distance / 1000).toDouble()).toInt()
                val location1 = locationList[i]
                val location2 = locationList[i + 1]
                val dLat = (location2.latitude - location1.latitude) / k
                val dLon = (location2.longitude - location1.longitude) / k
                for (cc in 0..k) {
                    val l = Location("A")
                    l.longitude = location1.longitude + cc * dLon
                    l.latitude = location1.latitude + cc * dLat
                    newLocationList.add(l)
                }
            } else {
                newLocationList.add(locationList[i])
            }
        }
        newLocationList.add(locationList.last())
        return newLocationList
    }

    private fun locationListener(locationList: List<Location>) {
        if (!locationDisposable.isDisposed) {
            locationDisposable.dispose()
        }
        locationDisposable = MapBoxStore.locationSubject
                .takeUntil(_onDestroy)
                .subscribe {
                    val i = getNearest(it, locationList)
                    val dist = calculateDist(locationList, it, i)
                    val msg = handler.obtainMessage(WHAT_DIST)
                    val bundle = Bundle()
                    bundle.putFloat(HANDLER_DIST_KEY, dist)
                    msg.data = bundle;
                    handler.sendMessageDelayed(msg, 10);

                }
    }

    private fun getNearest(current: Location, locationList: List<Location>): Int {
        var dist = Float.POSITIVE_INFINITY
        var i = 0
        locationList.forEachIndexed { index, it ->
            val d = it.distanceTo(current)
            if (d < dist) {
                dist = d
                i = index
            }
        }
        return i
    }

    private fun calculateDist(locationList: List<Location>, currentLocation: Location, index: Int): Float {
        var dist = 0.0f
        for (i in (index + 1)..locationList.size - 2) {
            dist += locationList[i].distanceTo(locationList[i + 1])
        }
        if (index + 1 <= locationList.size - 1) {
            dist += currentLocation.distanceTo(locationList[index + 1])
        }
        return dist / 1000
    }

    private fun getLocation(r: RoutePoints): Location {
        val l = Location("A")
        l.longitude = r.lon
        l.latitude = r.lat
        return l
    }


    private fun sendAltitude(altitude: Double) {
        val msg: Message = handler.obtainMessage(WHAT_ALT)
        val bundle = Bundle()
        bundle.putDouble(HANDLER_ALT_KEY, altitude)
        msg.data = bundle
        handler.sendMessageDelayed(msg, 40);
    }


    private fun sendParams(speed: Double, vario: Double, ratio: Double) {
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