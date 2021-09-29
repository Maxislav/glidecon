package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import com.atlas.mars.glidecon.R
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

@SuppressLint("ResourceType")
class MapRoute(val style: Style, val context: Context) {

    private val density: Float = Density(context).density

    companion object {
        const val SOURCE_ID = "route_source"
        const val LAYER_ID = "route_layer"
        const val SOURCE_AREA_POINT_ID = "turn_point_area_source"
        const val LAYER_AREA_POINT_ID = "turn_point_area_layer"
        const val TAG = "MapRoute"
        const val RADIUS = 500.0
    }

    init {
        val routeSource = createSource(SOURCE_ID)
        val routeLineColor = context.resources.getString(R.color.mapRouteColor)



        style.addLayer(LineLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(density * 2),
                PropertyFactory.lineOpacity(0.8f),
                PropertyFactory.lineColor(Color.parseColor(routeLineColor)),
        ))

        val routeCoordinates = getRouteCoordinates()
        routeSource.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                Feature.fromGeometry(LineString.fromLngLats(routeCoordinates))
        )))

        val areaSource = createSource(SOURCE_AREA_POINT_ID)
        val areaColor = context.resources.getString(R.color.mapRoutePointAreaColor)

        style.addLayer(LineLayer(LAYER_AREA_POINT_ID, SOURCE_AREA_POINT_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(density * 2),
                PropertyFactory.lineOpacity(0.5f),
                PropertyFactory.lineColor(Color.parseColor(areaColor)),
        ))
        val center1: Location = Location("A")
        center1.longitude = 30.0561
        center1.latitude = 50.4014

        val center2: Location = Location("A")
        center2.longitude = 29.8328
        center2.latitude = 50.5520

        val center3: Location = Location("A")
        center3.longitude = 29.473
        center3.latitude = 50.3346


        areaSource.setGeoJson(FeatureCollection.fromFeatures(
                arrayOf(
                        Feature.fromGeometry(LineString.fromLngLats( getPointArea(center1))),
                        Feature.fromGeometry(LineString.fromLngLats( getPointArea(center2))),
                        Feature.fromGeometry(LineString.fromLngLats( getPointArea(center3)))
                )
        ))

    }

    private fun getRouteCoordinates(): MutableList<Point> {
        val routeCoordinates = mutableListOf<Point>()

        routeCoordinates.add(Point.fromLngLat(30.0561, 50.4014))
        routeCoordinates.add(Point.fromLngLat(29.8328, 50.5520))
        routeCoordinates.add(Point.fromLngLat(29.4736, 50.3346))
        routeCoordinates.add(Point.fromLngLat(30.0561, 50.4014))
        return routeCoordinates
    }

    private fun getPointArea(center1: Location): MutableList<Point> {
        val routeCoordinates = mutableListOf<Point>()


        for (a in 0..360 step 5) {
            val loc = LocationUtil(center1).offset(RADIUS, a.toDouble())
            val p = Point.fromLngLat(loc.longitude, loc.latitude)
            routeCoordinates.add(p)
        }


        return routeCoordinates;
    }

    private fun createSource(sourceId: String): GeoJsonSource {
        style.addSource(GeoJsonSource(sourceId))
        return style.getSource(sourceId) as GeoJsonSource
    }
}