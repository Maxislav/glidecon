package com.atlas.mars.glidecon.model

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.util.LocationUtil
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.Observable.interval
import io.reactivex.Observable.just
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class MyPositionMarker(val mapView: MapView, mapboxMap: MapboxMap, val style: Style, context: Context) {

    var previousLocation: Location? = null;
    private val markerSource: GeoJsonSource
    private var moveOnNextFn: (() -> Unit)? = null
    private var rotateNextFn: (() -> Unit)? = null
    private var movingLocation: Location? = null
    private var rotationBearing = 0.0f

    private val symbolLayer = SymbolLayer(MY_POSITION_MARKER_LAYER_ID, MY_POSITION_MARKER_SOURCE_ID)

    companion object {
        private const val WHAT_LOCATION = 1
        private const val WHAT_BEARING = 2
        private const val TAG = "MyPositionMarker"
        private const val MY_POSITION_MARKER_IMAGE_ID = "MY_POSITION_MARKER_IMAGE_ID"
        private const val MY_POSITION_MARKER_SOURCE_ID = "MY_POSITION_MARKER_SOURCE_ID"
        private const val MY_POSITION_MARKER_LAYER_ID = "MY_POSITION_MARKER_LAYER_ID"
    }


    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_LOCATION -> {
                    val l = msg.obj as Location
                    setMarkerPosition(l)
                }
                WHAT_BEARING -> {
                    val b = msg.obj as Float
                    setMarkerRotation(b)
                }
            }
        }
    }

    private val _onDestroy = AsyncSubject.create<Boolean>()

    init {
        val myImage = MyImage(context)
        val bitmap: Bitmap = myImage.iconPlane
        style.addImage(MY_POSITION_MARKER_IMAGE_ID, bitmap);
        markerSource = createSource()
        // val symbolLayer = SymbolLayer(MY_POSITION_MARKER_LAYER_ID, MY_POSITION_MARKER_SOURCE_ID)

        val j: JsonElement = JsonParser.parseString("{'duration': 0, 'delay': 0}")
        val jj = object: Object() {
            var duration = 0.0f
            var delay = 0.0f
        }
        symbolLayer.setProperties(
                PropertyFactory.iconImage(MY_POSITION_MARKER_IMAGE_ID),
                PropertyFactory.iconSize(1.0f),
                PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
                PropertyValue("icon-ignore-placement", true),
                PropertyValue("icon-allow-overlap", true),
                PropertyValue("z-offset", 120),
               // PropertyValue("icon-opacity-transition", jj),
        )
        symbolLayer.iconColorTransition = TransitionOptions(1, 1)
        symbolLayer.iconOpacityTransition = TransitionOptions(1, 1)
        style.addLayer(symbolLayer);
        MapBoxStore.locationSubject
                .takeUntil(_onDestroy)
                .map { location ->


                    /* val singleFeatureOne = Feature.fromGeometry(
                             Point.fromLngLat(location.longitude, location.latitude)
                     )

                     markerSource.setGeoJson(singleFeatureOne)*/
                    Pair(previousLocation, location)
                }
                .subscribeBy(
                        onNext = {
                            if (it.first == null) {
                                val msg = handler.obtainMessage(WHAT_LOCATION, it.second)
                                handler.sendMessage(msg)
                            } else {
                                if (movingLocation == null) {
                                    movingLocation = it.first
                                }
                                moveOnNextFn?.let { it() }
                                movingLocation?.let { ml ->
                                    moveOnNextFn = moveNext(ml, it.second)
                                }

                            }

                            if (it.first == null) {
                                //  val bearing = 0.0f
                                val msg = handler.obtainMessage(WHAT_BEARING, rotationBearing)
                                handler.sendMessage(msg)
                            } else {
                                rotateNextFn?.let { it() }

                                movingLocation?.let { m ->
                                    val bb = m.bearingTo(it.second)
                                    val lu = LocationUtil()
                                    rotateNextFn = rotateNext(lu.bearingNormalize(rotationBearing), lu.bearingNormalize(bb))
                                }
                            }


                            /* it.first?.let(fun(prev: Location) {
                                 val bearing: Float = prev.bearingTo(it.second)
                                 symbolLayer.setProperties(PropertyFactory.iconRotate(bearing))
                             })*/
                            previousLocation = it.second
                        }
                )

    }


    private fun setMarkerPosition(l: Location) {
        val singleFeatureOne = Feature.fromGeometry(
                Point.fromLngLat(l.longitude, l.latitude)
        )

        markerSource.setGeoJson(singleFeatureOne)
    }

    private fun setMarkerRotation(bearing: Float) {
        symbolLayer.setProperties(PropertyFactory.iconRotate(bearing))
    }

    private fun moveNext(fromLocation: Location, toLocation: Location): () -> Unit {

        var stepLat = 0.0
        var stepLon = 0.0
        val l = just(1)
                .subscribeOn(Schedulers.newThread())
                .doOnNext {
                    val lat1 = fromLocation.latitude
                    val lat2 = toLocation.latitude
                    val lon1 = fromLocation.longitude
                    val lon2 = toLocation.longitude
                    stepLat = (lat2 - lat1) / 15
                    stepLon = (lon2 - lon1) / 15
                }
                .switchMap { interval(40, TimeUnit.MILLISECONDS) }
                .takeUntil(_onDestroy)
                .takeWhile { it < 15 }
                .subscribeBy {
                    val newLocation: Location = Location("A")
                    newLocation.latitude = fromLocation.latitude + stepLat * it
                    newLocation.longitude = fromLocation.longitude + stepLon * it
                    movingLocation = newLocation
                    val msg = handler.obtainMessage(WHAT_LOCATION, movingLocation)
                    handler.sendMessage(msg)
                    //  Log.d(TAG, "$it")
                }
        return fun() {
            l.dispose()
        }
    }

    private fun rotateNext(fromAngle: Float, toAngle: Float): () -> Unit {

        Log.d(TAG, "$fromAngle, $toAngle")
        var step = 1.0f;
        //val lu = LocationUtil()
        val l = just(1)
                .subscribeOn(Schedulers.newThread())
                .doOnNext {
                    step = if (180 < fromAngle - toAngle) {
                        (toAngle + 360 - fromAngle) / 10
                    } else if (fromAngle - toAngle < -180) {
                        (toAngle - fromAngle - 360) / 10
                    } else {
                        (toAngle - fromAngle) / 10
                    }

                }
                .switchMap { interval(35, TimeUnit.MILLISECONDS) }
                .takeUntil(_onDestroy)
                .takeWhile { it < 10 }
                .subscribeBy {
                    val b: Float = fromAngle + step * it
                    rotationBearing = b;
                    val msg = handler.obtainMessage(WHAT_BEARING, b)
                    handler.sendMessage(msg)
                }
        return fun() {
            l.dispose()
        }
    }

    private fun createSource(): GeoJsonSource {
        style.addSource(GeoJsonSource(MY_POSITION_MARKER_SOURCE_ID))
        return style.getSource(MY_POSITION_MARKER_SOURCE_ID) as GeoJsonSource
    }

    fun onDestroy() {
        _onDestroy.onComplete()
        handler.removeCallbacksAndMessages(null)
    }

    val isSourceExist: Boolean get() = style.getSource(MY_POSITION_MARKER_SOURCE_ID) != null
    /*fun isSourceExist(): Boolean{
        return style.getSource(MY_POSITION_MARKER_SOURCE_ID) != null
    }*/
}