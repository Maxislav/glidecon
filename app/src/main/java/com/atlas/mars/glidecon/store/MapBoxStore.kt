package com.atlas.mars.glidecon.store

import android.location.Location
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class MapBoxStore {

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
        lateinit var zoomControlSubject: BehaviorSubject<Zoom>
        lateinit var landingLiftToDragRatioSubject: BehaviorSubject<Map<LandingLiftToDragRatio, Double>>
        lateinit var landingBoxAngleSubject: BehaviorSubject<Double>
        lateinit var landingStartPointSubject: BehaviorSubject<LatLng>
        lateinit var defineStartingPointClickSubject: BehaviorSubject<Boolean>
        lateinit var routeBuildProgress: BehaviorSubject<Boolean>
        lateinit var activeRoute: BehaviorSubject<Double>
        lateinit var routeType: BehaviorSubject<RouteType>
        lateinit var routeButtonClick: PublishSubject<RouteAction>

        fun onCreate() {
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
            zoomControlSubject = BehaviorSubject.create()
            landingLiftToDragRatioSubject = BehaviorSubject.create()
            landingBoxAngleSubject = BehaviorSubject.createDefault(0.0)
            landingStartPointSubject = BehaviorSubject.create()
            defineStartingPointClickSubject = BehaviorSubject.create()
            routeBuildProgress = BehaviorSubject.createDefault(false)
            activeRoute = BehaviorSubject.create()
            routeType = BehaviorSubject.create()
            routeButtonClick = PublishSubject.create()
        }
        fun onDestroy(){
            mapboxMapSubject.onComplete()
            compassOnClickSubject.onComplete()
            cameraPositionSubject.onComplete()
            optimalSpeedSubject.onComplete()
            liftToDragRatioSubject.onComplete()
            startAltitudeSubject.onComplete()
            windSubject.onComplete()
            followTypeSubject.onComplete()
            locationSubject.onComplete()
            satelliteSubject.onComplete()
            tiltSubject.onComplete()
            zoomControlSubject.onComplete()
            landingLiftToDragRatioSubject.onComplete()
            landingBoxAngleSubject.onComplete()
            defineStartingPointClickSubject.onComplete()
            landingStartPointSubject.onComplete()
            routeBuildProgress.onComplete()
            activeRoute.onComplete()
            routeType.onComplete()
            routeButtonClick.onComplete()
        }
    }


    enum class Zoom {
        IN, OUT
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

    enum class LandingLiftToDragRatio {
        FLY, FINAL
    }

    enum class RouteAction(var routeAction: String) {
        BACK("back"),
        SAVE("save"),
        CLOSE("close");

        companion object {
            fun from(value: String): RouteAction {
                val dd = values().find { v -> v.routeAction == value }
                return dd!!
            }
        }
    }

    enum class PointType(var type: String){
        ROUTE("route"),
        TURN("turn");
        companion object {
            fun from(value: String): PointType {
                val dd = values().find { v -> v.type == value }
                return dd!!
            }
        }
    }

    enum class RouteType(var routeType: Int) {
        PLANE(0),
        BIKE(1),
        CAR(2);

        companion object {
            fun from(value: Int): RouteType {
                val dd = values().find { v -> v.routeType == value }
                return dd!!
            }
        }
    }
}