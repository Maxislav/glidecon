package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.util.Log
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.store.MapBoxStore
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


@SuppressLint("ResourceType")
class DirectionArea(val mapView: MapView, mapboxMap: MapboxMap, val style: Style, context: Context) {

    private var isSubscribed = true
    val TAG = "DirectionArea"
    // private val


    init {
        val source = createSource()
        // style.addSource( GeoJsonSource("djdjsj"))
        val c = context.getResources().getString(R.color.redBorderDark)
        style.addLayerBelow(
                FillLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(
                                fillColor(Color.parseColor(c)),
                                fillOpacity(0.5f)
                        ), "settlement-label")
        var location1: Location? = null
        MapBoxStore.locationSubject
                .takeWhile { isSubscribed }
                // .buffer(2)
                .subscribeBy { location2 ->
                    // Log.d(TAG, "$list")

                    //\ = list[0]
                    // val location2 = list[1]
                    if (location1 != null) {
                        val bearing = location1!!.bearingTo(location2)
                        var b1 = bearing.toDouble() - 5
                        if (b1 < 0) {
                            b1 = 360 + b1
                        }

                        val locationOffset1 = LocationUtil(location2).offset(2000.0, b1)
                        val locationOffset2 = LocationUtil(location2).offset(2000.0, bearing.toDouble() + 5)

                        // val locacion = LocationUtil(list.get(1))

                        val POINTS = mutableListOf<Point>()
                        POINTS.add(Point.fromLngLat(location2.longitude, location2.latitude))
                        POINTS.add(Point.fromLngLat(locationOffset1.longitude, locationOffset1.latitude))
                        POINTS.add(Point.fromLngLat(locationOffset2.longitude, locationOffset2.latitude))
                        val DD = mutableListOf<MutableList<Point>>()
                        DD.add(POINTS)
                        source.setGeoJson(Polygon.fromLngLats(DD))
                    }


                    location1 = location2
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
        val SOURCE_ID = "source-direction"
        const val LAYER_ID = "layer-direction-area"
    }
}