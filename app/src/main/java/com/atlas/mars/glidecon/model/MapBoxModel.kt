package com.atlas.mars.glidecon.model

import com.atlas.mars.glidecon.store.MapBoxStore.Companion.mapboxMapSubject
import com.mapbox.mapboxsdk.maps.MapboxMap
import io.reactivex.rxkotlin.subscribeBy

class MapBoxModel() {
    init {
        mapboxMapSubject.subscribeBy (
            onNext = { mapboxMap ->
                initMap(mapboxMap)
            }
        )
    }

    private fun initMap(mapboxMap: MapboxMap){

    }
}