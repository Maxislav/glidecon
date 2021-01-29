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
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.cameraPosition
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.compassOnClickSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.followTypeSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.locationSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.mapboxMapSubject
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import java.util.*
import kotlin.math.pow


class MapBoxModel(val mapView: MapView, val context: Context) {
    private val TAG = "MapBoxModel"
    private var isSubscribed = true
    lateinit var markerViewManager: MarkerViewManager

    init {


        initTouchListener()
        // mapView?.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap: MapboxMap ->

            mapboxMapSubject.onNext(mapboxMap)


            // mapboxMapSubject.onComplete()

            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style: Style ->

                MyPositionMarker(mapView, mapboxMap, style, context)


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
                .subscribeBy(
                        onNext = { mapboxMap ->
                            initMap(mapboxMap)
                        }
                )
    }

    private fun initMap(mapboxMap: MapboxMap) {

        cameraPosition.onNext(mapboxMap.cameraPosition)
        mapboxMap.addOnCameraMoveListener(object : MapboxMap.OnCameraMoveListener {
            override fun onCameraMove() {
                cameraPosition.onNext(mapboxMap.cameraPosition)
            }
        })

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

        /* followSubject
                 .withLatestFrom(locationSubject, (isSubscribed) -> isSubscribed)*/


        Observables.combineLatest(followTypeSubject, locationSubject)
                .takeWhile { isSubscribed }
                .filter(fun(pair): Boolean {
                    return pair.first != MapBoxStore.FollowViewType.TYPICAL
                })
                .map { pair -> pair.second }
                .subscribeBy(
                        onNext = { location ->
                            val latLng = LatLng(location.latitude, location.longitude)

                            // val builder =
                            val position = CameraPosition.Builder()
                                    .target(latLng)
                                    .build()
                            mapboxMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(position));
                        }
                )
        /*followSubject
                .takeWhile { isSubscribed }
                .withLatestFrom(locationSubject) { ignore, dialogContents -> Pair(ignore, dialogContents) }
                .filter(fun(pair): Boolean {
                    return pair.first
                })
                .map{pair -> pair.second}
                .subscribeBy(
                        onNext = { location ->
                            val latLng = LatLng(location.latitude, location.longitude)
                            val position = CameraPosition.Builder()
                                    .target(latLng)
                                    .build()
                            mapboxMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(position));
                        }
                )*/


        /*locationSubject
                .takeWhile { isSubscribed }
                .subscribeBy(
                        onNext = { location ->
                            val latLng = LatLng(location.latitude, location.longitude)
                            val position = CameraPosition.Builder()
                                    .target(latLng)
                                    .build()
                            mapboxMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(position));
                        }
                )*/

        //CameraPosition position = new CameraPosition.Builder()
        // mapboxMap.setBe
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
}

