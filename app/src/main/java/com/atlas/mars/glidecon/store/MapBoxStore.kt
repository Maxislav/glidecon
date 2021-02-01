package com.atlas.mars.glidecon.store

import android.location.Location
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import io.reactivex.subjects.BehaviorSubject

class MapBoxStore {
    companion object {
        val followTypeSubject: BehaviorSubject<FollowViewType> = BehaviorSubject.createDefault(FollowViewType.TYPICAL)
        val compassOnClickSubject: BehaviorSubject<Boolean> = BehaviorSubject.create()
        val cameraPosition: BehaviorSubject<CameraPosition> = BehaviorSubject.create()
        val mapboxMapSubject: BehaviorSubject<MapboxMap> = BehaviorSubject.create()
        val locationSubject: BehaviorSubject<Location> = BehaviorSubject.create()
        val satelliteSubject: BehaviorSubject<Map<SatCount, Int>> = BehaviorSubject.createDefault(mapOf(SatCount.TOTAl to 0, SatCount.USED  to 0))
    }

    enum class SatCount{
        TOTAl, USED
    }
    enum class FollowViewType{
        TYPICAL, FOLLOW, FOLLOW_ROTATE
    }
}