package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.database.MapDateBase
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.util.LocationUtil
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import kotlinx.coroutines.*

interface RoutePoints {
    val lat: Double
    val lon: Double
    val type: MapBoxStore.PointType
}

class MapRouteActive(val style: Style, val context: Context) {
    companion object {
        const val SOURCE_ID = "route_source_active"
        const val LAYER_ID = "route_layer_active"
        const val SOURCE_AREA_POINT_ID = "turn_point_area_source_active"
        const val LAYER_AREA_POINT_ID = "turn_point_area_layer_active"

        private const val POINT_LAYER_ID = "ACTIVE_POINT_LAYER_ID"
        private const val POINT_SOURCE_ID = "ACTIVE_POINT_SOURCE_ID"
        private const val POINT_IMAGE_ID = "ACTIVE_POINT_IMAGE_ID"
        const val RADIUS = 500.0
        private const val WHAT_DRAW_POINTS = 1;
        private const val WHAT_CLEAR_POINTS = 2;
    }
    private val TAG = "MapRouteActive"
    val mapDateBase = MapDateBase(context)
    private val density: Float = Density(context).density
    private val _onDestroy = AsyncSubject.create<Boolean>()

    private val areaSource = createSource(SOURCE_AREA_POINT_ID)
    private val routeSource = createSource(SOURCE_ID)
    private val pointSource = createSource(POINT_SOURCE_ID)

    private val routeTurnPointList = mutableListOf<LatLng>()
    private val routeFullPointList = mutableListOf<LatLng>()

    @ObsoleteCoroutinesApi
    private val scope = CoroutineScope(newSingleThreadContext(TAG))

    private val handler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            routeTurnPointList.clear()
            routeFullPointList.clear()
            when (msg.what) {
                WHAT_DRAW_POINTS -> {
                    val list = msg.obj as MutableList<RoutePoints>

                    list.forEach {
                        val latLng: LatLng = LatLng(it.lat, it.lon)
                        if (it.type === MapBoxStore.PointType.ROUTE) {
                            routeFullPointList.add(latLng)
                        }
                        if (it.type == MapBoxStore.PointType.TURN) {
                            routeTurnPointList.add(latLng)
                        }
                    }
                }
                WHAT_CLEAR_POINTS -> {
                    routeTurnPointList.clear()
                    routeFullPointList.clear()
                }
            }
            setAreaSource(routeTurnPointList)
            setLineSource(routeFullPointList)

        }
    }

    init {
        val routeLineColor = context.resources.getString(R.color.mapRouteColorActive)

        val myImage = MyImage(context)
        val turnPointBitmap: Bitmap = myImage.getMarkerPoint(20, R.color.mapRouteColorActive, 1.0f)
        style.addImage(POINT_IMAGE_ID, turnPointBitmap)
        style.addLayer(SymbolLayer(POINT_LAYER_ID, POINT_SOURCE_ID).withProperties(
                PropertyFactory.iconImage(POINT_IMAGE_ID),
                PropertyFactory.iconSize(1.0f),
                PropertyFactory.fillOpacity(0.6f),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
        ))

        style.addLayer(LineLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(density * 2),
                PropertyFactory.lineOpacity(0.8f),
                PropertyFactory.lineColor(Color.parseColor(routeLineColor)),
        ))

        val areaColor = context.resources.getString(R.color.mapRoutePointAreaColorActive)

        style.addLayer(LineLayer(LAYER_AREA_POINT_ID, SOURCE_AREA_POINT_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(density * 2),
                PropertyFactory.lineOpacity(0.4f),
                PropertyFactory.lineColor(Color.parseColor(areaColor)),
        ))


        MapBoxStore.activeRoute
                .takeUntil(_onDestroy)
                .subscribeOn(Schedulers.newThread())
                .subscribeBy {
                    getRoutePoints(it)
                }


        /* val helloWorld: A = object: A {
             override val h = "Hello"

             // object expressions extend Any, so `override` is required on `toString()`
            //  override fun toString() = "$hello $world"
         }
         dd(helloWorld)*/
    }
    /* fun dd(vv: A){
         Log.d(TAG, vv.h)
     }*/


    private fun getRoutePoints(id: Number) {
        if (-1 < id.toInt()) {

            val list = mapDateBase.getRoutePoints(id)
            val msg = handler.obtainMessage(WHAT_DRAW_POINTS, list)
            handler.sendMessage(msg)
        } else {
            handler.sendEmptyMessage(WHAT_CLEAR_POINTS)
        }


    }


    private fun createSource(sourceId: String): GeoJsonSource {
        style.addSource(GeoJsonSource(sourceId))
        return style.getSource(sourceId) as GeoJsonSource
    }


    private fun setAreaSource(routeTurnPointList: MutableList<LatLng>) {
        val featureList = mutableListOf<Feature>()
        val pointList = mutableListOf<Feature>()
        routeTurnPointList.forEach { latLng ->
            val c = Location("A")
            c.longitude = latLng.longitude
            c.latitude = latLng.latitude
            val feature = Feature.fromGeometry(LineString.fromLngLats(getPointArea(c)))
            featureList.add(feature)
            val singleFeatureOne = Feature.fromGeometry( Point.fromLngLat(
                    latLng.longitude,
                    latLng.latitude
            ) )
            pointList.add(singleFeatureOne)
        }
        areaSource.setGeoJson(FeatureCollection.fromFeatures(featureList))
        pointSource.setGeoJson(FeatureCollection.fromFeatures(pointList))
    }

    private fun setLineSource(routeFullPointList: MutableList<LatLng>) {
        val pointList = mutableListOf<Point>()
        routeFullPointList.forEach { latLng ->
            val p = Point.fromLngLat(latLng.longitude, latLng.latitude)
            pointList.add(p)
        }

        routeSource.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                Feature.fromGeometry(LineString.fromLngLats(pointList))
        )))
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

    fun onDestroy() {
        _onDestroy.onComplete()
    }


}