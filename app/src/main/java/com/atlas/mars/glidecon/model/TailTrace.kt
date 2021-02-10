package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.store.MapBoxStore
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.rxkotlin.subscribeBy
import java.util.*


@SuppressLint("ResourceType")
class TailTrace(val style: Style, context: Context) {
    private var isSubscribed = true
    private val locationList = mutableListOf<Location>()

    companion object {
        const val TAIL_LENGTH = 1000.0f
        const val SOURCE_ID = "source-tail-trace"
        const val LAYER_ID = "layer-tail-trace"
        const val TAG = "DirectionArea"
    }

    init {
        val source = createSource(SOURCE_ID)
        val criticalColor = context.resources.getString(R.color.safetyColor)



        style.addLayer(LineLayer(LAYER_ID, SOURCE_ID).withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(10f),
                lineGradient(interpolate(
                        linear(), lineProgress(),
                        stop(0.0f, rgb(255, 87, 37)),  // yellow
                        stop(0.2f, rgb(255, 30, 0)), // red
                        stop(1.0f, rgba(255, 30, 0, 0)),





                ))))

       /* source.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                Feature.fromGeometry(LineString.fromLngLats(routeCoordinates))
        )))*/
        MapBoxStore.locationSubject
                .takeWhile { isSubscribed }
                .doOnNext { locationList.add(it) }
                .buffer(2, 1)
                .filter { buff -> 1 < buff.size }
                .map { getCoordinates() }
                .subscribeBy { routeCoordinates ->
                   /// source.
                    routeCoordinates.reverse();
                    source.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                            Feature.fromGeometry(LineString.fromLngLats(routeCoordinates))
                    )))
                }
    }

    private fun getCoordinates(): MutableList<Point> {
        val routeCoordinates = mutableListOf<Point>()

        while (TAIL_LENGTH < calcTailLength()) {
            locationList.removeAt(0)
        }

        for (loc in locationList) {
            val p = Point.fromLngLat(loc.longitude, loc.latitude)
            routeCoordinates.add(p)
        }

        return routeCoordinates
    }

    private fun calcTailLength(): Float {
        var distance = 0.0f
        for (i in 0..locationList.size - 2) {
            val previousLocation = locationList[i]
            val currentLocation = locationList[i + 1]
            distance += previousLocation.distanceTo(currentLocation)
        }
        return distance
    }


    private fun createSource(sourceId: String): GeoJsonSource {
        style.addSource(GeoJsonSource(sourceId, GeoJsonOptions().withLineMetrics(true)))
        return style.getSource(sourceId) as GeoJsonSource
    }

    fun onDestroy() {
        isSubscribed = false
    }
}