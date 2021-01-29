package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.cameraPosition
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.compassOnClickSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.followSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.locationSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.mapboxMapSubject
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import kotlin.math.pow

class MapBoxModel(val mapView: MapView) {
    private val TAG = "MapBoxModel"
    private var isSubscribed = true


    init {


        initTouchListener()
        // mapView?.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap: MapboxMap ->
            mapboxMapSubject.onNext(mapboxMap)
            // mapboxMapSubject.onComplete()

            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                val uiSettings = mapboxMap.uiSettings
                uiSettings
                // mapboxMap.uiSettings.setAttributionEnabled(false)
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
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


        Observables.combineLatest(followSubject, locationSubject)
                .takeWhile { isSubscribed }
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
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val distX = startX?.minus(X!!)
                        val distY = startY?.minus(Y!!)

                        val sum = distY?.let { distX?.toDouble()?.pow(2.0)?.plus(it.pow(2)) }
                        val res = sum?.pow(0.5)
                        if (followSubject.value && 100 < res!!) {
                            followSubject.onNext(false)
                        }
                        Log.d(TAG, "${distX}")
                    }
                }

                return false
            }

        })
    }

    fun onDestroy() {
        isSubscribed = false
    }
}

