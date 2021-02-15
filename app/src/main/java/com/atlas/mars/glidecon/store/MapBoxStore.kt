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
        tiltSubject = BehaviorSubject.create()

    }

    companion object {
        lateinit var mapboxMapSubject: BehaviorSubject<MapboxMap>
        lateinit var compassOnClickSubject: BehaviorSubject<Boolean>
        lateinit var cameraPositionSubject: BehaviorSubject<CameraPosition>
        lateinit var optimalSpeedSubject: BehaviorSubject<Double>
        lateinit var liftToDragRatioSubject: BehaviorSubject<Double>
        lateinit var startAltitudeSubject: BehaviorSubject<Double>
        lateinit var windSubject: BehaviorSubject<Map<Wind, Double>>
        lateinit var followTypeSubject: BehaviorSubject<FollowViewType>
        lateinit var locationSubject: BehaviorSubject<Location>
        lateinit var satelliteSubject: BehaviorSubject<Map<SatCount, Int>>
        lateinit var tiltSubject: BehaviorSubject<Int>
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