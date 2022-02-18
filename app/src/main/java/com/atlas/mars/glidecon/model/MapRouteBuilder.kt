package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.database.MapDateBase
import com.atlas.mars.glidecon.dialog.DialogSaveTrack
import com.atlas.mars.glidecon.dialog.OpenFileDialog
import com.atlas.mars.glidecon.rest.RouteRequest
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.util.LoadFile
import com.atlas.mars.glidecon.util.LocationUtil
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.Observable.just
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory


@ObsoleteCoroutinesApi
@SuppressLint("ResourceType")
class MapRouteBuilder(val style: Style, val context: Context) {

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

    private val pointSource = createSource(POINT_SOURCE_ID)

    var loading = false

    @ObsoleteCoroutinesApi
    private val scope = CoroutineScope(newSingleThreadContext(TAG))
    private val handler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_SAVE -> {
                    MapBoxStore.routeBuildProgress.onNext(false)
                    val id = msg.obj as Int
                    MapBoxStore.activeRoute.onNext(id)
                }
                WHAT_READ -> {
                    setAreaSource()
                    setLineSource()
                }
            }
        }
    }

    companion object {
        const val SOURCE_ID = "route_source"
        const val LAYER_ID = "route_layer"
        const val SOURCE_AREA_POINT_ID = "turn_point_area_source"
        const val LAYER_AREA_POINT_ID = "turn_point_area_layer"

        private const val POINT_LAYER_ID = "BUILDER_POINT_LAYER_ID"
        private const val POINT_SOURCE_ID = "BUILDER_POINT_SOURCE_ID"
        private const val POINT_IMAGE_ID = "BUILDER_POINT_IMAGE_ID"
        private const val TAG = "MapRoute"
        private const val RADIUS = 500.0
        private const val WHAT_SAVE = 1
        private const val WHAT_READ = 2
    }

    init {
        val myImage = MyImage(context)
        val turnPointBitmap: Bitmap = myImage.getMarkerPoint(20, R.color.mapRouteColor, 1.0f)
        val routeLineColor = context.resources.getString(R.color.mapRouteColor)
        style.addImage(POINT_IMAGE_ID, turnPointBitmap);

        style.addLayer(LineLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(density * 2),
                PropertyFactory.lineOpacity(0.8f),
                PropertyFactory.lineColor(Color.parseColor(routeLineColor)),
        ))
        val j: JsonArray = JsonArray()
        val jjo = JsonObject()
        jjo.addProperty("duration", 0.0)
        jjo.addProperty("delay", 0.0)

        // j.add(JsonParser.parseString("{'duration': 0, 'delay': 0}"))
        // j.add(JsonParser.parseString("[\"interpolate\",[\"duration\",\"0\"]]"))

        val jjjs = JsonParser.parseString("[\"interpolate\", ['duration', 0]]")

        // JsonArray =
        class AA {
            val duration = 0
            val delay = 0
        }
        Expression.literal(j)

        val jjj = HashMap<String, Int>();
        jjj.put("duration", 0)

        val fdf = SymbolLayer(POINT_LAYER_ID, POINT_SOURCE_ID).withProperties(
                PropertyFactory.iconImage(POINT_IMAGE_ID),
                PropertyFactory.iconSize(1.0f),
                PropertyValue("icon-ignore-placement", true),
                PropertyValue("icon-allow-overlap", true),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
        )
        fdf.setIconColorTransition(TransitionOptions(1, 1))
        fdf.setIconOpacityTransition(TransitionOptions(1, 1))
        style.addLayer(fdf)


        val areaColor = context.resources.getString(R.color.mapRoutePointAreaColor)

        style.addLayer(LineLayer(LAYER_AREA_POINT_ID, SOURCE_AREA_POINT_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(density * 2),
                PropertyFactory.lineOpacity(0.2f),
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
                    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                    val defaultName = df.format(Date())

                    val d = DialogSaveTrack(context, defaultName) { trackName: String ->
                        onSave(trackName)
                    }
                    d.create().show()
                }

        MapBoxStore.routeButtonClick
                .takeUntil(_onDestroy)
                .filter {
                    it === MapBoxStore.RouteAction.DOWNLOAD
                }
                .subscribe {
                    val fileDialog = OpenFileDialog(context, getCurrentPath())
                    fileDialog.setOpenDialogListener(object : OpenFileDialog.OpenDialogListener {
                        override fun onSelectedFile(fileName: String?) {
                            fileName?.let {
                                if (it.matches(Regex(".+kml$", RegexOption.IGNORE_CASE))) {
                                    readFileKml(it)
                                } else if (it.matches(Regex(".+gpx$", RegexOption.IGNORE_CASE))) {
                                    readFileGpx(it)
                                }
                            }
                        }

                        override fun onSelectPath(filePath: String?) {
                            if (!filePath.isNullOrBlank()) {
                                Log.d(TAG, (filePath))
                                saveCurrentPath(filePath)
                            }

                        }

                    })
                    fileDialog.show()
                }

    }


    private fun mapDefined(mapBoxMap: MapboxMap) {
        var defineRoutePoint: DefineRoutePoint? = null
        // latLngToContainerPoint

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
        override fun onMapLongClick(latLng: LatLng): Boolean {
            Log.d(TAG, "map click")
            if (loading) {
                return true
            }
            MapBoxStore.routeType
                    .takeUntil(_onDestroy)
                    .take(1)
                    .subscribeBy {

                        // mapboxMap.projection.toScreenLocation()

                        when (it!!) {
                            MapBoxStore.RouteType.PLANE -> {
                                routeTurnPointList.add(latLng)
                                routeFullPointList.add(latLng)

                                setAreaSource()
                                setLineSource()
                                steps.add(1)
                            }

                            MapBoxStore.RouteType.CAR, MapBoxStore.RouteType.BIKE -> {

                                if (0 < routeTurnPointList.size) {
                                    routeTurnPointList.add(latLng)
                                    setAreaSource()
                                    val r = RouteRequest()
                                    loading = true
                                    r.restCar(routeTurnPointList[routeTurnPointList.size - 2], latLng, it) { list, err ->
                                        if (err === null && list !== null) {
                                            for (i in 0..list.size - 2 step 2) {
                                                val latLng: LatLng = LatLng(list[i], list[i + 1])
                                                routeFullPointList.add(latLng)
                                            }
                                            steps.add(list.size / 2)
                                            setLineSource()

                                        } else if (err !== null) {
                                            routeTurnPointList.removeLast()
                                            setAreaSource()
                                            setLineSource()
                                            Log.d(TAG, err.stackTraceToString())
                                            TToast(context as Activity).show("Server error", TToast.Type.ERROR, 3000)

                                        } else {
                                            routeTurnPointList.removeLast()
                                            setAreaSource()
                                            setLineSource()
                                            TToast(context as Activity).show("Server error", TToast.Type.ERROR, 3000)
                                        }
                                        loading = false
                                    }
                                } else {
                                    routeTurnPointList.add(latLng)
                                    routeFullPointList.add(latLng)
                                    setAreaSource()
                                    setLineSource()
                                    steps.add(1)
                                }
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

                    val trackPointList = mutableListOf<TrackPoint>()
                    routeTurnPointList.forEach { p ->
                        trackPointList.add(TrackPoint(p, MapBoxStore.PointType.TURN))
                    }
                    routeFullPointList.forEach { p ->
                        trackPointList.add(TrackPoint(p, MapBoxStore.PointType.ROUTE))
                    }
                    val dist = calcDistance(routeFullPointList)
                    val id = mapDateBase.saveTrackName(trackName, dist)
                    mapDateBase.saveTrackPoints(id, trackPointList)
                    val msg = handler.obtainMessage(WHAT_SAVE, id.toInt())
                    handler.sendMessage(msg)
                }

    }

    private fun calcDistance(list: MutableList<LatLng>): Double {
        var dist = 0.0
        for (i in 0 until list.size - 1) {
            val a = list[i]
            val b = list[i + 1]
            dist += a.distanceTo(b)
        }
        return String.format("%.1f", dist / 1000).toDouble();
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
        val pointList = mutableListOf<Feature>()
        routeTurnPointList.forEach { latLng ->
            val c = Location("A")
            c.longitude = latLng.longitude
            c.latitude = latLng.latitude
            val feature = Feature.fromGeometry(LineString.fromLngLats(getPointArea(c)))
            featureList.add(feature)

            val singleFeatureOne = Feature.fromGeometry(Point.fromLngLat(
                    latLng.longitude,
                    latLng.latitude
            ))
            pointList.add(singleFeatureOne)
        }
        areaSource.setGeoJson(FeatureCollection.fromFeatures(featureList))
        pointSource.setGeoJson(FeatureCollection.fromFeatures(pointList))


        // pointSource.setGeoJson(singleFeatureOne)
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

    private fun readFileGpx(path: String){
        GlobalScope.launch(Dispatchers.IO) {
            val text = LoadFile(path).text
            val co = mutableListOf<LatLng>()
            text?.let {
                val inputStream: InputStream = text.byteInputStream()
                val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val doc = db.parse(inputStream)
                var latLngs = arrayOfNulls<LatLng>(0)

                var i = 0
                var k = 0
                var count = 0
                val trkseg = doc.getElementsByTagName("trkseg")
                for(i in 0 until trkseg.length){
                    val element = trkseg.item(i) as Element
                    val trkpt = element.getElementsByTagName("trkpt")
                    for(k in 0..trkpt.length){
                        count++
                    }
                }
                latLngs = arrayOfNulls(count)
                count = 0
                for(i in 0 until trkseg.length){
                    val element = trkseg.item(i) as Element
                    val trkpt = element.getElementsByTagName("trkpt")
                    for (k in 0 until trkpt.length){
                        val ff = trkpt.item(k) as Element
                        val lat = ff.getAttribute("lat")
                        val lng = ff.getAttribute("lon")
                        latLngs[count] = LatLng(lat.toDouble(), lng.toDouble())
                        count++
                    }
                }
                for(latLng in latLngs){
                    latLng?.let{co.add(it)}
                }
                routeTurnPointList.add(co.first())
                routeTurnPointList.add(co.last())
                for (latLng in co) {
                    routeFullPointList.add(latLng)
                }
                steps.add(co.size)
                handler.sendEmptyMessage(WHAT_READ)

            }
        }
    }

    private fun readFileKml(path: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val text = LoadFile(path).text
            val co = mutableListOf<LatLng>()
            text?.let {
                val inputStream: InputStream = text.byteInputStream()
                val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val doc = db.parse(inputStream)
                val coordinatesList: NodeList = doc.getElementsByTagName("coordinates")

                for (i in 0 until coordinatesList.length) {
                    val n = coordinatesList.item(i)
                    val coordinates = n.textContent
                    val coordinatesStringList = coordinates.split(Regex("\\s+")).filter { 0 < it.length }
                    if (1 < coordinatesStringList.size) {
                        coordinatesStringList.forEach { st ->
                            val l = st.split(Regex(","))
                            val latLng = LatLng(l[1].toDouble(), l[0].toDouble())
                            co.add(latLng)
                        }
                    }
                }
            }
            routeTurnPointList.add(co.first())
            routeTurnPointList.add(co.last())
            for (latLng in co) {
                routeFullPointList.add(latLng)
            }
            steps.add(co.size)
            handler.sendEmptyMessage(WHAT_READ)
        }
    }

    private fun saveCurrentPath(path: String) {
        if (2 < path.length) {
            val sharedPreferences = context.getSharedPreferences("DATA", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("READ_PATH", path).apply()
        }
    }

    private fun getCurrentPath(): String? {
        val sharedPreferences = context.getSharedPreferences("DATA", Context.MODE_PRIVATE)
        return sharedPreferences.getString("READ_PATH", null)
    }
}