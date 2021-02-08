package com.atlas.mars.glidecon.store

import android.location.Location
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.maps.MapboxMap
import io.reactivex.subjects.BehaviorSubject

class MapBoxStore {

    init {
        mapboxMapSubject = BehaviorSubject.create()
        compassOnClickSubject = BehaviorSubject.create()
        cameraPositionSubject = BehaviorSubject.create()
        optimalSpeedSubject = BehaviorSubject.create()
        liftToDragRatioSubject = BehaviorSubject.create()
        startAltitudeSubject = BehaviorSubject.create()
        windSubject = BehaviorSubject.create()
        followTypeSubject = BehaviorSubject.createDefault(FollowViewType.TYPICAL)
        locationSubject = BehaviorSubject.create()
        satelliteSubject = BehaviorSubject.createDefault(mapOf(SatCount.TOTAl to 0, SatCount.USED to 0))
    }

    companion object {
        var mapboxMapSubject: BehaviorSubject<MapboxMap> = BehaviorSubject.create()
        var compassOnClickSubject: BehaviorSubject<Boolean> = BehaviorSubject.create()
        var cameraPositionSubject: BehaviorSubject<CameraPosition> = BehaviorSubject.create()
        var optimalSpeedSubject: BehaviorSubject<Double> = BehaviorSubject.create()
        var liftToDragRatioSubject: BehaviorSubject<Double> = BehaviorSubject.create()
        var startAltitudeSubject: BehaviorSubject<Double> = BehaviorSubject.create()
        var windSubject: BehaviorSubject<Map<Wind, Double>> = BehaviorSubject.create()
        var followTypeSubject: BehaviorSubject<FollowViewType> = BehaviorSubject.createDefault(FollowViewType.TYPICAL)
        var locationSubject: BehaviorSubject<Location> = BehaviorSubject.create()
        var satelliteSubject: BehaviorSubject<Map<SatCount, Int>> = BehaviorSubject.createDefault(mapOf(SatCount.TOTAl to 0, SatCount.USED to 0))
    }

    enum class Wind {
        DIRECTION, SPEED
    }

    enum class SatCount {
        TOTAl, USED
    }

    enum class FollowViewType {
        TYPICAL, FOLLOW, FOLLOW_ROTATE
    }
}