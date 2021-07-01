package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.util.LocationUtil
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.cos

@SuppressLint("ResourceType")
class LandingPatternBox(val style: Style, val context: Context) {
    var isSubscribe = true
    private var handler: Handler
    private val density: Float = Density(context).density

    companion object {
        const val SOURCE_ID = "landing_pattern_source"
        const val LAYER_ID = "landing_pattern_layer"
        const val speed = 100.0 / 3.6 //m/s
        const val innerAltitude = 300.0
        const val TAG = "LandingPatternBox"
    }

    init {
        val criticalSource = createSource(SOURCE_ID)
        val criticalColor = context.resources.getString(R.color.landingPatternColor)


        style.addLayer(LineLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(density*3),
                PropertyFactory.lineOpacity(0.8f),
                PropertyFactory.lineColor(Color.parseColor(criticalColor)),
        ))

        handler = object : Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            override fun handleMessage(msg: Message) {
               when(msg.what){
                   1 -> {
                       val routeCoordinates = msg.obj as MutableList<Point>
                       criticalSource.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                               Feature.fromGeometry(LineString.fromLngLats(routeCoordinates))
                       )))
                   }
               }
            }
        }

        Observables.combineLatest(
                MapBoxStore.landingLiftToDragRatioSubject,
                MapBoxStore.windSubject,
                MapBoxStore.landingBoxAngleSubject,
                MapBoxStore.landingStartPointSubject,
                ) { a, b, c, d -> Triple(a, b, c) }
                .takeWhile { isSubscribe }
                .debounce(40, TimeUnit.MILLISECONDS)
                .map {
                    calcRatioList(it.first, it.second, it.third)
                }
                .map {
                    getKDist(it)
                    val startPoint = Point.fromLngLat(
                            MapBoxStore.landingStartPointSubject.value.longitude,
                            MapBoxStore.landingStartPointSubject.value.latitude
                    )
                    getPatternCoordinates(
                            startPoint,
                            MapBoxStore.landingBoxAngleSubject.value,
                            getKDist(it) )
                }
                .subscribeBy(
                        onNext = { routeCoordinates ->
                            val msg = handler.obtainMessage(1, routeCoordinates)
                            handler.sendMessage(msg)
                        }
                )
    }

    fun onDestroy() {
        isSubscribe = false
    }
    private fun getPatternCoordinates(startPoint: Point, direction: Double, a: Double): MutableList<Point>{
        val routeCoordinates = mutableListOf<Point>()
        val location = LocationUtil()
        location.longitude = startPoint.longitude()
        location.latitude = startPoint.latitude()
        val l1 = location.offset(0.1*a, direction)
        routeCoordinates.add(l1.toPoint())

        val l2 = l1.offset(a*0.7, direction-90)
        routeCoordinates.add(l2.toPoint())

        val l3 = l2.offset(a, direction-180)
        routeCoordinates.add(l3.toPoint())

        val l4 = l3.offset(0.7*a, direction-270)
        routeCoordinates.add(l4.toPoint())

        routeCoordinates.add(location.toPoint())

        // routeCoordinates.add(l3.toPoint())
        return routeCoordinates;
    }

    private fun createSource(sourceId: String): GeoJsonSource {
        style.addSource(GeoJsonSource(sourceId))
        return style.getSource(sourceId) as GeoJsonSource
    }
    private fun calcRatioList(sourceRatio: Map<MapBoxStore.LandingLiftToDragRatio, Double>, wind: Map<MapBoxStore.Wind, Double>, startDirection: Double): List<Double> {
        val windSpeed = wind[MapBoxStore.Wind.SPEED] // 7
        val windDirection = wind[MapBoxStore.Wind.DIRECTION]
        val ratioFly = sourceRatio[MapBoxStore.LandingLiftToDragRatio.FLY]
        val ratioFlyFinal = sourceRatio[MapBoxStore.LandingLiftToDragRatio.FINAL]
        var slideWind = 0.0;

        slideWind = LocationUtil().bearingNormalize(startDirection - 90) - windDirection!!
        val k12 = getK(ratioFly!!, slideWind, windSpeed!!)

        slideWind = LocationUtil().bearingNormalize(startDirection - 180) - windDirection
        val k23 = getK(ratioFly!!, slideWind, windSpeed!!)

        slideWind = LocationUtil().bearingNormalize(startDirection + 90) - windDirection
        val k34 = getK(ratioFly!!, slideWind, windSpeed!!)

        slideWind = LocationUtil().bearingNormalize(startDirection) - windDirection
        val k4 = getK(ratioFlyFinal!!, slideWind, windSpeed!!)

        Log.d(TAG, "ratio list $k12 $k23")
        Log.d(TAG, "ratio list $k34 $k4")
        return listOf(k12, k23, k34, k4)

    }

    private fun getKDist(list: List<Double>): Double {
        val r = list.reduce { acc, d ->
            acc * d
        }
        return 300.0 * r / (0.7 * r / list[0] + 1.0 * r / list[1] + 0.7 * r / list[2] + 0.9 * r / list[3])
    }

    private fun getK(ratioFly: Double, slideWind: Double, windSpeed: Double): Double {
        var _slideWind = LocationUtil().bearingNormalize(180 - slideWind)
        if (180 < _slideWind) {
            _slideWind -= 360
        }
        var k12 = ratioFly
        val actualSpeed = speed - windSpeed * cos(Math.toRadians(_slideWind))
        ratioFly?.let {
            k12 = it * actualSpeed / speed
        }
        return k12
    }
}