package com.atlas.mars.glidecon.store

import android.location.Location
import com.mapbox.mapboxsdk.maps.MapboxMap
import io.reactivex.subjects.BehaviorSubject

class MapBoxStore {
    companion object {
        val mapboxMapSubject: BehaviorSubject<MapboxMap> = BehaviorSubject.create()
        val locationSubject: BehaviorSubject<Location> = BehaviorSubject.create()
        val satelliteSubject: BehaviorSubject<Map<String, Int>> = BehaviorSubject.createDefault(mapOf("total" to 0, "used" to 0))
        // val mapboxMapSubject: PublishSubject<MapboxMap> =  PublishSubject.create()
    }
}