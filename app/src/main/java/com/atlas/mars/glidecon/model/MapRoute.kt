package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.database.MapDateBase
import com.atlas.mars.glidecon.dialog.DialogLendingBox
import com.atlas.mars.glidecon.dialog.DialogSaveTrack
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.util.LocationUtil
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.Observable.just
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.util.*

@ObsoleteCoroutinesApi
@SuppressLint("ResourceType")
class MapRoute(val style: Style, val context: Context) {

    private val TAG = "MapRoute";
    val mapDateBase = MapDateBase(context)

    private val density: Float = Density(context).density

    private val _onDestroy = AsyncSubject.create<Boolean>()
    private val routePointSubject = BehaviorSubject.create<Array<Feature>>()
    private val routeTurnPointList = mutableListOf<LatLng>()
    private val routeFullPointList = mutableListOf<LatLng>()
    private val steps = mutableListOf<Int>()
    private val areaSource = createSource(SOURCE_AREA_POINT_ID)
    private val routeSource = createSource(SOURCE_ID)

    @ObsoleteCoroutinesApi
    private val scope = CoroutineScope(newSingleThreadContext(TAG))
    private val handler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {
                    MapBoxStore.routeBuildProgress.onNext(false)
                }
            }
        }
    }

    companion object {
        const val SOURCE_ID = "route_source"
        const val LAYER_ID = "route_layer"
        const val SOURCE_AREA_POINT_ID = "turn_point_area_source"
        const val LAYER_AREA_POINT_ID = "turn_point_area_layer"
        const val TAG = "MapRoute"
        const val RADIUS = 500.0
    }

    init {

        val routeLineColor = context.resources.getString(R.color.mapRouteColor)



        style.addLayer(LineLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(density * 2),
                PropertyFactory.lineOpacity(0.8f),
                PropertyFactory.lineColor(Color.parseColor(routeLineColor)),
        ))

        //val routeCoordinates = getRouteCoordinates()
        /*routeSource.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                Feature.fromGeometry(LineString.fromLngLats(routeCoordinates))
        )))*/


        val areaColor = context.resources.getString(R.color.mapRoutePointAreaColor)

        style.addLayer(LineLayer(LAYER_AREA_POINT_ID, SOURCE_AREA_POINT_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(density * 2),
                PropertyFactory.lineOpacity(0.5f),
                PropertyFactory.lineColor(Color.parseColor(areaColor)),
        ))

        MapBoxStore.mapboxMapSubject
                .takeUntil(_onDestroy)
                .take(1)
                .subscribe {
                    mapDefined(it)
                }

        MapBoxStore.routeBuildProgress
                .takeUntil(_onDestroy)
                .filter { !it }
                .subscribeBy {
                    routeTurnPointList.clear()
                    routeFullPointList.clear()
                    steps.clear()
                    setAreaSource()
                    setLineSource()
                }

        MapBoxStore.routeButtonClick
                .takeUntil(_onDestroy)
                .filter {
                    it === MapBoxStore.RouteAction.BACK
                }
                .subscribeBy {
                    stepBack()
                }

        MapBoxStore.routeButtonClick
                .takeUntil(_onDestroy)
                .filter {
                    it === MapBoxStore.RouteAction.SAVE
                }
                .subscribeBy {
                    val d = DialogSaveTrack(context) { trackName: String ->
                        onSave(trackName)
                    }
                    d.create().show()
                }

    }


    private fun mapDefined(mapBoxMap: MapboxMap) {
        var defineRoutePoint: DefineRoutePoint? = null
        MapBoxStore.routeBuildProgress
                .takeUntil(_onDestroy)
                .subscribeBy {
                    if (it) {
                        defineRoutePoint = DefineRoutePoint()
                        mapBoxMap.addOnMapLongClickListener(defineRoutePoint!!)
                    } else {
                        defineRoutePoint?.let {
                            mapBoxMap.removeOnMapLongClickListener(defineRoutePoint!!)
                        }
                    }
                }
    }


    inner class DefineRoutePoint : MapboxMap.OnMapLongClickListener {
        override fun onMapLongClick(point: LatLng): Boolean {
            Log.d(TAG, "map click")
            MapBoxStore.routeType
                    .takeUntil(_onDestroy)
                    .take(1)
                    .subscribeBy {
                        when (it!!) {
                            MapBoxStore.RouteType.PLANE -> {
                                routeTurnPointList.add(point)
                                routeFullPointList.add(point)

                                setAreaSource()
                                setLineSource()
                                steps.add(1)
                            }
                            MapBoxStore.RouteType.BIKE -> {

                            }
                            MapBoxStore.RouteType.CAR -> {

                            }
                        }
                    }
            return true
        }
    }

    private fun onSave(trackName: String) {
        just(1)
                .subscribeOn(Schedulers.newThread())
                .subscribeBy {
                    val id = mapDateBase.saveTrackName(trackName)
                    val trackPointList = mutableListOf<TrackPoint>()
                    routeTurnPointList.forEach { p ->
                        trackPointList.add(TrackPoint(p, MapBoxStore.PointType.TURN))
                    }
                    routeFullPointList.forEach { p ->
                        trackPointList.add(TrackPoint(p, MapBoxStore.PointType.ROUTE))
                    }
                    mapDateBase.saveTrackPoints(id, trackPointList)
                    handler.sendEmptyMessage(1)
                }

    }

    private fun stepBack() {
        if (0 < steps.size) {
            val n = steps.removeLast()
            routeTurnPointList.removeLast()
            for (i in 1..n) {
                routeFullPointList.removeLast()
            }
            setAreaSource()
            setLineSource()
            // routeFullPointList.sc
        }


    }

    private fun setAreaSource() {
        val featureList = mutableListOf<Feature>()
        routeTurnPointList.forEach { latLng ->
            val c = Location("A")
            c.longitude = latLng.longitude
            c.latitude = latLng.latitude
            val feature = Feature.fromGeometry(LineString.fromLngLats(getPointArea(c)))
            featureList.add(feature)
        }
        areaSource.setGeoJson(FeatureCollection.fromFeatures(featureList))
    }

    private fun setLineSource() {
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

    private fun createSource(sourceId: String): GeoJsonSource {
        style.addSource(GeoJsonSource(sourceId))
        return style.getSource(sourceId) as GeoJsonSource
    }

    fun onDestroy() {
        _onDestroy.onComplete()
        handler.removeCallbacksAndMessages(null);
    }

}