package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.atlas.mars.glidecon.database.MapDateBase
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.cameraPositionSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.compassOnClickSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.followTypeSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.locationSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.mapboxMapSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.tiltSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.zoomControlSubject
import com.atlas.mars.glidecon.util.LocationUtil
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import java.util.*
import kotlin.math.pow


class MapBoxModel(val mapView: MapView, val context: Context, val myViewModel: LandingBoxViewModel) {
    private val TAG = "MapBoxModel"
    private var isSubscribed = true
    lateinit var markerViewManager: MarkerViewManager

    lateinit var directionArea: DirectionArea
    lateinit var flightRadius: FlightRadius
    lateinit var tailTrace: TailTrace
    lateinit var landingPatternBox: LandingPatternBox
    lateinit var mapRouteBuilder: MapRouteBuilder
    lateinit var mapRouteActive: MapRouteActive

    var mapboxMap: MapboxMap? = null
    val mapDateBase: MapDateBase = MapDateBase(context)
    var defineStartingPoint: DefineStartingPoint? = null

    init {


        initTouchListener()
        // mapView?.onCreate(savedInstanceState)

        context.applicationContext

        mapView.getMapAsync { mapboxMap: MapboxMap ->

            mapboxMapSubject.onNext(mapboxMap)


            // mapboxMapSubject.onComplete()

            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style: Style ->
                tailTrace = TailTrace(style, context)
                landingPatternBox = LandingPatternBox(style, context)
                mapRouteBuilder = MapRouteBuilder(style, context)
                mapRouteActive = MapRouteActive(style, context)

                MyPositionMarker(mapView, mapboxMap, style, context)
                directionArea = DirectionArea(mapView, mapboxMap, style, context)
                flightRadius = FlightRadius(style, context)


                // TODO влияет на тачскрин зараза val symbolManager = SymbolManager(mapView, mapboxMap, style)


            }
        }
        mapboxMapSubject
                .takeWhile { isSubscribed }
                .subscribeBy(
                        onNext = { value: MapboxMap ->
                            Log.d(TAG, "mapboxMap defined")
                        }
                )
        MapBoxStore.locationSubject
                .takeWhile { isSubscribed }
                .subscribeBy(
                        onNext = { location: Location ->
                            Log.d(TAG, "location ${location.toString()}")
                        }
                )
        val list = listOf(mapboxMapSubject, MapBoxStore.locationSubject)

        val ff = Observables.combineLatest(mapboxMapSubject, MapBoxStore.locationSubject)
                .subscribeBy(
                        onNext = { pair: Pair<MapboxMap, Location> ->


                        }
                )

        mapView.addOnCameraWillChangeListener(object : MapView.OnCameraWillChangeListener {
            override fun onCameraWillChange(animated: Boolean) {

            }

        })
        mapboxMapSubject
                //  .last(1)
                .takeWhile { isSubscribed }
                .subscribeBy(
                        onNext = { mapboxMap ->
                            initMap(mapboxMap)
                            //   mapboxMapSubject.onComplete()
                        }
                )


    }

    private fun initMap(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        MapBoxStore.defineStartingPointClickSubject
                .takeWhile { isSubscribed }
                .filter{defineStartingPoint == null}
                .subscribeBy {
                    defineStartingPoint = DefineStartingPoint()
                    mapboxMap.addOnMapLongClickListener(defineStartingPoint!!)
                }

        val cp = mapDateBase.getCameraPosition()
        cp?.let {
            mapboxMap.cameraPosition = cp
            cameraPositionSubject.onNext(cp)
        }
        tiltSubject
                .takeWhile { isSubscribed }
                .subscribeBy(
                        onNext = {
                            val tilt: Double = (60 * it / 100).toDouble()
                            val position = CameraPosition.Builder()
                                    .tilt(tilt)
                                    .build()
                            mapboxMap.cameraPosition = position
                        }
                )

        zoomControlSubject
                .takeWhile { isSubscribed }
                .subscribeBy { zoom: MapBoxStore.Zoom ->
                    var currentZoom = mapboxMap.cameraPosition.zoom
                    when (zoom) {
                        MapBoxStore.Zoom.IN -> {
                            currentZoom += 0.5

                        }
                        MapBoxStore.Zoom.OUT -> {
                            currentZoom -= 0.5
                        }
                    }
                    val position = CameraPosition.Builder()
                            .zoom(currentZoom)
                            .build()
                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position));
                    //mapboxMap.cameraPosition = position
                }

        mapboxMap.addOnCameraMoveListener(object : MapboxMap.OnCameraMoveListener {
            override fun onCameraMove() {
                cameraPositionSubject.onNext(mapboxMap.cameraPosition)
            }
        })
        initCameraPositionListener(mapboxMap)
        mapboxMap.addOnMoveListener(object : MapboxMap.OnMoveListener {

            var locationA: Location? = null

            override fun onMoveBegin(detector: MoveGestureDetector) {
                locationA = Location("start")
                locationA?.latitude = mapboxMap.cameraPosition.target.latitude
                locationA?.longitude = mapboxMap.cameraPosition.target.latitude
                Log.d(TAG, "onMoveBegin")
            }

            override fun onMove(detector: MoveGestureDetector) {
                Log.d(TAG, "onMove")

            }

            override fun onMoveEnd(detector: MoveGestureDetector) {
                Log.d(TAG, "onMoveEnd")
            }

        })



        compassOnClickSubject
                .takeWhile { isSubscribed }
                .subscribeBy(
                        onNext = {
                            val position = CameraPosition.Builder()
                                    .bearing(0.0)
                                    .build()
                            mapboxMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(position));
                        }
                )


    }

    private fun initCameraPositionListener(mapboxMap: MapboxMap) {
        var previousLocation: Location? = null;
        var previousTime: Long? = null// = System.currentTimeMillis()
        Observables.combineLatest(followTypeSubject, locationSubject)
                .takeWhile { isSubscribed }
                .filter(fun(pair): Boolean {
                    return pair.first != MapBoxStore.FollowViewType.TYPICAL
                })
                .subscribeBy(
                        onNext = { pair ->
                            val followViewType = pair.first;
                            val location = pair.second
                            val latLng = LatLng(location.latitude, location.longitude)
                            val position: CameraPosition

                            if (previousLocation != null && followViewType == MapBoxStore.FollowViewType.FOLLOW_ROTATE) {
                                val bearing = LocationUtil().bearingNormalize(previousLocation!!.bearingTo(location).toDouble()) //  - 180

                                /*Log.d(TAG, "bering to $bearing")
                                if (bearing < 0) {
                                    bearing += 360
                                }
                                bearing -= 180;
                                if (bearing < 0) {
                                    bearing += 360
                                }*/

                                Log.d(TAG, "bearing = $bearing")
                                position = CameraPosition.Builder()
                                        .bearing((bearing).toDouble())
                                        .target(latLng)
                                        .build()
                            } else {
                                position = CameraPosition.Builder()
                                        .target(latLng)
                                        .build()
                            }

                            //val time = previousTime?.minus(System.currentTimeMillis())?.let { Math.abs(it) }
                            val time = previousTime?.let { previous -> System.currentTimeMillis() - previous }


                            mapboxMap.easeCamera(CameraUpdateFactory
                                    .newCameraPosition(position), time?.let { if (1000 < it) 1000 else it.toInt() }
                                    ?: 1000, false);

                            previousLocation = location
                            previousTime = System.currentTimeMillis()
                        }
                )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTouchListener() {
        mapView.setOnTouchListener(object : View.OnTouchListener {
            var startX: Float? = 0.0F
            var startY: Float? = 0.0F


            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val X: Float? = event?.rawX
                val Y: Float? = event?.rawY
                when (event!!.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = X
                        startY = Y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val distX = startX?.minus(X!!)
                        val distY = startY?.minus(Y!!)
                        val pow1 = distX?.toDouble()?.pow(2.0)
                        val pow2 = distY?.toDouble()?.pow(2.0)
                        val res = (pow1!! + pow2!!).pow(0.5)
                        Log.d(TAG, "${res}")
                        if (followTypeSubject.value !== MapBoxStore.FollowViewType.TYPICAL && 100 < res!!) {
                            followTypeSubject.onNext(MapBoxStore.FollowViewType.TYPICAL)
                        }
                    }
                }

                return false
            }

        })
    }

    fun onDestroy() {
        isSubscribed = false
        // mapboxMapSubjectReset()
        mapboxMap?.let { mapDateBase.saveCameraPosition(it.cameraPosition) }

        directionArea.onDestroy()
        flightRadius.onDestroy()
        tailTrace.onDestroy()
        landingPatternBox.onDestroy()
        mapRouteBuilder.onDestroy()
        mapRouteActive.onDestroy()
    }

    internal class DrawView(context: Context?) : View(context) {
        var paint: Paint
        var bitmap: Bitmap
        override fun onDraw(canvas: Canvas) {
            canvas.drawARGB(80, 102, 204, 255)
            canvas.drawBitmap(bitmap, 50.5.toFloat(), 50.5.toFloat(), paint)
        }

        init {
            paint = Paint(Paint.ANTI_ALIAS_FLAG)
            bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
            bitmap.setPixel(20, 20, Color.RED)
            bitmap.setPixel(70, 50, Color.RED)
            bitmap.setPixel(30, 80, Color.RED)
            val colors = IntArray(10 * 15)
            Arrays.fill(colors, 0, 10 * 15, Color.GREEN)
            bitmap.setPixels(colors, 0, 10, 40, 40, 10, 15)
            val canvas = Canvas(bitmap)
            val p = Paint()
            p.setColor(Color.BLUE)
            canvas.drawCircle(80.toFloat(), 80.toFloat(), 10.toFloat(), p)
        }
    }

    inner class DefineStartingPoint : MapboxMap.OnMapLongClickListener {
        override fun onMapLongClick(point: LatLng): Boolean {
            defineStartingPoint?.let {
                mapboxMap?.removeOnMapLongClickListener (it)
                defineStartingPoint = null
            }

            myViewModel.setStartLatLng(point)
            /*val dialogWindSetting = DialogWindSetting(context)
            dialogWindSetting.create().show()*/
            return true
        }
    }

    /*override fun onMapLongClick(point: LatLng): Boolean {
        TODO("Not yet implemented")
    }*/
}

