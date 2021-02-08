package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.liftToDragRatioSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.locationSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.optimalSpeedSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.startAltitudeSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.windSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Wind
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
import io.reactivex.rxkotlin.subscribeBy


import io.reactivex.rxkotlin.Observables

@SuppressLint("ResourceType")
class FlightRadius(val style: Style, context: Context) {
    private var isSubscribed = true
    companion object {
        const val CRITICAL_SOURCE_ID = "source-flight-area"
        const val CRITICAL_LAYER_ID = "critical-layer-flight-area"
        const val SAFETY_SOURCE_ID = "safety-source-flight-area"
        const val SAFETY_LAYER_ID = "safety-layer-flight-area"
        const val TAG = "DirectionArea"
    }

    init {
        val criticalSource = createSource(CRITICAL_SOURCE_ID)
        val criticalColor = context.resources.getString(R.color.safetyColor)
        style.addLayer(LineLayer(CRITICAL_LAYER_ID, CRITICAL_SOURCE_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(3f),
                PropertyFactory.lineOpacity(0.35f),
                PropertyFactory.lineColor(Color.parseColor(criticalColor)),
               /* PropertyFactory.lineGradient(
                        interpolate(
                                linear(), lineProgress(),
                                stop(0f, rgb(6, 1, 255)), // blue
                                stop(0.1f, rgb(59, 118, 227)), // royal blue
                                stop(0.3f, rgb(7, 238, 251)), // cyan
                                stop(0.5f, rgb(0, 255, 42)), // lime
                                stop(0.7f, rgb(255, 252, 0)), // yellow
                                stop(1f, rgb(255, 30, 0)) // red
                        ))*/


        ))

        val safetySource = createSource(SAFETY_SOURCE_ID)
        val safetyModeColor = context.resources.getString(R.color.safetyColor)
        style.addLayer(LineLayer(SAFETY_LAYER_ID, SAFETY_SOURCE_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(3f),
                PropertyFactory.lineOpacity(0.5f),
                PropertyFactory.lineColor(Color.parseColor(safetyModeColor)),


        ))


        Observables.combineLatest(
                optimalSpeedSubject,
                liftToDragRatioSubject,
                windSubject,
                startAltitudeSubject,
                locationSubject
        ) { speed, ratio, wind, startAltitude, location ->
            getCircleCoordinates(speed, ratio, wind, startAltitude, location)
        }
                .takeWhile { isSubscribed }
                .subscribeBy { routeCoordinates ->
                    criticalSource.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                            Feature.fromGeometry(LineString.fromLngLats(routeCoordinates))
                    )))
                }
        Observables.combineLatest(
                optimalSpeedSubject,
                liftToDragRatioSubject,
                windSubject,
                startAltitudeSubject,
                locationSubject
        ) { speed, ratio, wind, startAltitude, location ->
            getCircleCoordinates(speed, ratio, wind, startAltitude+300, location)
        }
                .takeWhile { isSubscribed }
                .subscribeBy { routeCoordinates ->
                    safetySource.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                            Feature.fromGeometry(LineString.fromLngLats(routeCoordinates))
                    )))
                }


        optimalSpeedSubject
                .takeWhile { isSubscribed }
                .flatMap { locationSubject }
                .subscribeBy {

                }
        locationSubject
                .takeWhile { isSubscribed }
                .concatMap {
                    optimalSpeedSubject
                }
                //.mergeWith{it -> optimalSpeedSubject}

                .subscribeBy {


                    /*val routeCoordinates = mutableListOf<Point>()
                    routeCoordinates.add(Point.fromLngLat(30.5, 50.5))
                    routeCoordinates.add(Point.fromLngLat(it.longitude, it.latitude))
                    source.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                            Feature.fromGeometry(LineString.fromLngLats(routeCoordinates))
                    )))*/
                }


    }

    private fun getCircleCoordinates(
            speed: Double,
            ratio: Double,
            wind: Map<MapBoxStore.Wind, Double>,
            startAltitude: Double,
            location: Location,
    ): MutableList<Point> {
        val routeCoordinates = mutableListOf<Point>()
        val verticalSpeed = speed / 3.6 / ratio
        val timeToLand = (location.altitude - startAltitude) / verticalSpeed
        val offset = wind[Wind.SPEED]?.let { timeToLand * it }
        val offsetDirection: Double? = wind[Wind.DIRECTION]
        var center: Location = Location("A")
        if (offset !== null && offsetDirection != null) {
            center = LocationUtil(location).offset(offset, offsetDirection)
        }
        val radius = timeToLand * speed / 3.6
        for (a in 0..360 step 5) {
            val loc = LocationUtil(center).offset(radius, a.toDouble())
            val p = Point.fromLngLat(loc.longitude, loc.latitude)
            routeCoordinates.add(p)
        }
        return routeCoordinates
    }

    private fun createSource(sourceId: String): GeoJsonSource {
        style.addSource(GeoJsonSource(sourceId))
        return style.getSource(sourceId) as GeoJsonSource
    }


    fun onDestroy() {
        isSubscribed = false
    }



}