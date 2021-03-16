package com.atlas.mars.glidecon.model

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import com.atlas.mars.glidecon.store.MapBoxStore
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.rxkotlin.subscribeBy

class MyPositionMarker(val mapView: MapView, mapboxMap: MapboxMap, val style: Style, context: Context) {
    private val MY_POSITION_MARKER_IMAGE_ID = "my_position_marker_image_id"
    private val MY_POSITION_MARKER_SOURCE_ID = "MY_POSITION_MARKER_SOURCE_ID"
    private val MY_POSITION_MARKER_LAYER_ID = "MY_POSITION_MARKER_LAYER_ID"
    var previousLocation: Location? = null;
    private val markerSource: GeoJsonSource

    private var isSubscribe = true;

    init {
        val myImage = MyImage(context)
        val bitmap: Bitmap = myImage.iconPlane
        style.addImage(MY_POSITION_MARKER_IMAGE_ID, bitmap);
        markerSource = createSource()
        val symbolLayer = SymbolLayer(MY_POSITION_MARKER_LAYER_ID, MY_POSITION_MARKER_SOURCE_ID)
        symbolLayer.setProperties(
                PropertyFactory.iconImage(MY_POSITION_MARKER_IMAGE_ID),
                PropertyFactory.iconSize(1.0f),
                PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
        )
        style.addLayer(symbolLayer);
        MapBoxStore.locationSubject
                .subscribeBy(
                        onNext = { location: Location ->

                            previousLocation
                            val singleFeatureOne = Feature.fromGeometry(
                                    Point.fromLngLat(location.longitude,
                                            location.latitude))
                            // val dd: GeoJsonSource = style.getSource(MY_POSITION_MARKER_SOURCE_ID) as GeoJsonSource

                            markerSource.setGeoJson(singleFeatureOne)

                            val bearing: Float? = previousLocation?.bearingTo(location)

                            if(bearing!= null){
                                symbolLayer.setProperties( PropertyFactory.iconRotate(bearing))
                            }
                            previousLocation = location

                                    // previousLocation = location;

                        }
                )

    }

    private fun createSource(): GeoJsonSource {
        style.addSource(GeoJsonSource(MY_POSITION_MARKER_SOURCE_ID))
        return style.getSource(MY_POSITION_MARKER_SOURCE_ID) as GeoJsonSource
    }

    fun onDestroy(){
        isSubscribe = false
    }

    val isSourceExist: Boolean get() = style.getSource(MY_POSITION_MARKER_SOURCE_ID) != null
    /*fun isSourceExist(): Boolean{
        return style.getSource(MY_POSITION_MARKER_SOURCE_ID) != null
    }*/
}