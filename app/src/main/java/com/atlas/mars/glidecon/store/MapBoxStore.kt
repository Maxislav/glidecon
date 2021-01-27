package com.atlas.mars.glidecon.store

import android.location.Location
import com.mapbox.mapboxsdk.maps.MapboxMap
import io.reactivex.subjects.BehaviorSubject

class MapBoxStore {
    companion object {
        val mapboxMapSubject: BehaviorSubject<MapboxMap> = BehaviorSubject.create()
        val locationSubject: BehaviorSubject<Location> = BehaviorSubject.create()
        val satelliteSubject: BehaviorSubject<Map<SatCount, Int>> = BehaviorSubject.createDefault(mapOf(SatCount.TOTAl to 0, SatCount.USED  to 0))
        val satelliteSubject1: BehaviorSubject<Map<SatCount, Int>> = BehaviorSubject.createDefault(mapOf(SatCount.TOTAl to 0, SatCount.USED to 0))
        // val mapboxMapSubject: PublishSubject<MapboxMap> =  PublishSubject.create()
    }

    enum class SatCount{
        TOTAl, USED
    }
}