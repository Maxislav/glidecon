package com.atlas.mars.glidecon.model

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.store.MapBoxStore
import com.google.android.gms.tasks.Tasks.await
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
import io.reactivex.subjects.AsyncSubject
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


@DelicateCoroutinesApi
@SuppressLint("ResourceType")
class TailTrace(val style: Style, val context: Context) {
    private val _onDestroy = AsyncSubject.create<Boolean>();
    private val locationList = mutableListOf<Location>();
    private val source = createSource(SOURCE_ID)
    private lateinit var lineLayer: LineLayer
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_LOCATION -> {
                    val l = msg.obj as MyCoordinates
                    source.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                            Feature.fromGeometry(LineString.fromLngLats(l.list))
                    )))
                    lineLayer.setProperties(
                            lineGradient(interpolate(linear(), lineProgress(), *l.stopList))
                    )
                }
            }
        }
    }

    companion object {
        const val TAIL_LENGTH = 2000.0f
        const val SOURCE_ID = "source-tail-trace"
        const val LAYER_ID = "layer-tail-trace"
        const val TAG = "DirectionArea"
        private const val WHAT_LOCATION = 1
    }

    init {

        lineLayer = LineLayer(LAYER_ID, SOURCE_ID).withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(20f),
                lineBlur(5.0f),
                lineGradient(interpolate(
                        linear(), lineProgress(),
                        stop(0.0f, hexToRgba(R.color.tailTrace1)),  // yellow
                        stop(0.02f, hexToRgba(R.color.tailTrace2)),  // yellow
                        stop(0.1f, hexToRgba(R.color.tailTrace3)),  // yellow
                        stop(0.2f, hexToRgba(R.color.tailTrace4)),  // yellow
                        stop(1.0f, hexToRgba(R.color.tailTrace5)),  // yellow
                )))

        style.addLayer(lineLayer)
        var j: Job? = null

        MapBoxStore.locationSubject
                .takeUntil(_onDestroy)
                .throttleWithTimeout(100, TimeUnit.MILLISECONDS)
                .doOnNext { locationList.add(it) }
                .buffer(2, 1)
                .filter { buff -> 1 < buff.size }
                .subscribe {
                    j?.let {
                        if(it.isActive) it.cancel()
                    }
                    j = GlobalScope.launch(Dispatchers.IO) {
                        val k = async { getCoordinates() }
                        val r = k.await()
                        val msg = handler.obtainMessage(WHAT_LOCATION, r)
                        handler.sendMessage(msg)
                    }
                    /*runBlocking {
                        val k = async { getCoordinates() }

                    }*/
                    /*val j = GlobalScope.launch(Dispatchers.IO) {
                        val k = async { getCoordinates() }
                        runBlocking{
                            var r = k.await()
                        }


                    }
                    j.invokeOnCompletion {

                    }*/
                }
        /*.map { getCoordinates() }
        .subscribeBy { pair ->
            source.setGeoJson(FeatureCollection.fromFeatures(arrayOf(
                    Feature.fromGeometry(LineString.fromLngLats(pair.first))
            )))
            lineLayer.setProperties(
                    lineGradient(interpolate(linear(), lineProgress(), *pair.second))
            )

        }*/
    }

    suspend fun funA(): Int {
        return 1
    }

    class MyCoordinates(val list: MutableList<Point>, val stopList: Array<Stop>) {

    }

    @Synchronized
    private suspend fun getCoordinates(): MyCoordinates {
        val routeCoordinates = mutableListOf<Point>()

        while (TAIL_LENGTH < calcTailLength()) {
            locationList.removeAt(0)
        }

        for (loc in locationList) {
            val p = Point.fromLngLat(loc.longitude, loc.latitude)
            routeCoordinates.add(p)
        }

        val paramList = mutableListOf<MutableMap<String, Float>>()
        var dist = 0.0f
        for (i in 0..locationList.size - 2) {
            val previousLocation = locationList[i]
            val currentLocation = locationList[i + 1]

            val dTime = currentLocation.time - previousLocation.time
            val vario = 1000 * (currentLocation.altitude - previousLocation.altitude) / dTime


            var colorIndex = 100 * (vario + 2.5) / 5
            if (colorIndex < 0) {
                colorIndex = 0.0
            } else if (100 < colorIndex) {
                colorIndex = 100.0
            }
            val dDist = previousLocation.distanceTo(currentLocation)
            dist += dDist
            val mm = mutableMapOf("dist" to dist, "vario" to vario.toFloat(), "colorIndex" to colorIndex.toFloat())
            paramList.add(mm)
        }
        var alist: Array<Stop> = arrayOf()
        for (param in paramList) {
            val st = param["dist"]?.div(dist)
            st?.let {
                param.set("stop", it)
                val colorIndex = param["colorIndex"]
                alist = alist.plus(arrayOf<Stop>(stop(it, getTailColor(colorIndex!!, it))))
            }
        }
        // return Pair(routeCoordinates, alist)
        return MyCoordinates(routeCoordinates, alist)
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

        return rgba(red, green, blue, alpha / 255)
    }

    /**
     * index  0 - 100
     */
    private fun getTailColor(index: Float, alpha: Float): Expression {
        var r: Int = 0;
        var g: Int = 0;
        var b: Int = 0;
        //  var a: Float = alpha
        if (index < 25) {
            r = 0
            //25 = 255
            //index  = x
            g = (255 * index / 25).toInt()
            b = 255
        } else if (index < 50) {
            r = 0
            g = 255
            b = 255 - (255 * (index - 25) / 25).toInt()
        } else if (index < 75) {
            g = 255
            b = 0
            r = (255 * (index - 50) / 25).toInt()
        } else {
            r = 255
            b = 0
            g = 255 - (255 * (index - 75) / 25).toInt()
        }

        return rgba(r, g, b, alpha)
    }

    fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        _onDestroy.onComplete()
    }
}