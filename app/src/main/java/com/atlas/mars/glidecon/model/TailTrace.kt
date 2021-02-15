package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.store.MapBoxStore
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.rxkotlin.subscribeBy
import java.util.*


@SuppressLint("ResourceType")
class TailTrace(val style: Style, val context: Context) {
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
                        stop(0.0f, hexToRgba(R.color.tailTrace1)),  // yellow
                        stop(0.02f,  hexToRgba(R.color.tailTrace2)),  // yellow
                        stop(0.1f,hexToRgba(R.color.tailTrace3)),  // yellow
                        stop(0.2f,hexToRgba(R.color.tailTrace4)),  // yellow
                        stop(1.0f, hexToRgba(R.color.tailTrace5)),  // yellow


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

    private fun hexToRgba(@ColorRes c: Int): Expression {
        val color: Int = ContextCompat.getColor(context, c) //context.resources.getColor(R.color.tailTrace1)
        val red = (color shr 16 and 0xFF).toFloat()
        val green = (color shr 8 and 0xFF).toFloat()
        val blue = (color and 0xFF).toFloat()
        val alpha = (color shr 24 and 0xFF).toFloat()

        return rgba(red, green, blue, alpha/255)
    }

    fun onDestroy() {
        isSubscribed = false
    }
}