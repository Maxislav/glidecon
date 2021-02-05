package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.util.Log
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.startAltitudeSubject
import com.atlas.mars.glidecon.util.LocationUtil
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.rxkotlin.subscribeBy
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon;
import io.reactivex.rxkotlin.Observables


@SuppressLint("ResourceType")
class DirectionArea(val mapView: MapView, mapboxMap: MapboxMap, val style: Style, context: Context) {

    private var isSubscribed = true

    // private val

    val locationList = mutableListOf<Location>()

    init {
        val source = createSource()
        // style.addSource( GeoJsonSource("djdjsj"))
        val c = context.resources.getString(R.color.blue)
        style.addLayerBelow(
                FillLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(
                                fillColor(Color.parseColor(c)),
                                fillOpacity(0.3f)
                        ), "settlement-label")
        //var previousLocation: Location? = null

        val ll = MapBoxStore.locationSubject
                .takeWhile { isSubscribed }
                .doOnNext { addLocation(it) }
                .buffer(2, 1)
                .filter { buff -> 1 < buff.size }


        Observables
                .combineLatest(startAltitudeSubject, ll)
                .subscribeBy {

                    val startAltitude = it.first
                    val locationListBuff = it.second
                    val previousLocation = locationListBuff[0]
                    val currentLocation = locationListBuff[1]


                    val k = 25
                    // todo uncomment
                    //val k = calcDragRatio()

                    if (0 < k) {
                        Log.d(TAG, "altitude, ${currentLocation.altitude}")
                        val dist = (currentLocation.altitude - startAltitude) * k
                        val bearing = previousLocation.bearingTo(currentLocation)
                        val b1 = LocationUtil().bearingNormalize(bearing.toDouble() - 5)
                        val b2 = LocationUtil().bearingNormalize(bearing.toDouble() + 5)


                        val locationOffset1 = LocationUtil(currentLocation).offset(dist, b1)
                        val locationOffset2 = LocationUtil(currentLocation).offset(dist, b2)
                        val points = mutableListOf<Point>()
                        points.add(Point.fromLngLat(currentLocation.longitude, currentLocation.latitude))
                        points.add(Point.fromLngLat(locationOffset1.longitude, locationOffset1.latitude))
                        points.add(Point.fromLngLat(locationOffset2.longitude, locationOffset2.latitude))
                        val coordinates = mutableListOf<MutableList<Point>>()
                        coordinates.add(points)
                        source.setGeoJson(Polygon.fromLngLats(coordinates))
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
            val dAltitude = currentLocation.altitude - previousLocation.altitude
            kList.add(distance / dAltitude)
        }
        var kSum = 0.0
        for (k in kList) {
            kSum += k
        }
        return kSum / kList.size
    }

    private fun addLocation(location: Location) {
        locationList.add(location)

        if (0 < locationList.size) {

            while (10000 < (locationList.last().time - locationList.first().time)) {
                locationList.removeAt(0)
            }
        }

    }

    private fun createSource(): GeoJsonSource {
        style.addSource(GeoJsonSource(SOURCE_ID))
        return style.getSource(SOURCE_ID) as GeoJsonSource
    }

    fun onDestroy() {
        isSubscribed = false
    }

    companion object {
        const val SOURCE_ID = "source-direction"
        const val LAYER_ID = "layer-direction-area"
        const val TAG = "DirectionArea"
    }
}