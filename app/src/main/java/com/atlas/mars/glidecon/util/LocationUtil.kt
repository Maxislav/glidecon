package com.atlas.mars.glidecon.util

import android.location.Location
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class LocationUtil() : Location("A") {


    constructor(location: Location) : this() {
        this.longitude = location.longitude
        this.latitude = location.latitude
        this.altitude = location.altitude
    }

    fun offset(location: Location, distance: Double, bearing: Double): Location {
        //var lat1 = toRadians(c1[1]);
        var bearing = toRadians(bearing)
        val lat1 = toRadians(location.latitude)
        // var lon1 = toRadians(c1[0]);
        val lon1 = toRadians(location.longitude);

        val dByR = distance / RADIUS; // distance divided by 6378137 (radius of the earth) wgs84

        val lat = Math.asin(
                Math.sin(lat1) * Math.cos(dByR) + Math.cos(lat1) * Math.sin(dByR) * Math.cos(bearing)
        );
        val lon =
                lon1 +
                        Math.atan2(
                                Math.sin(bearing) * Math.sin(dByR) * Math.cos(lat1),
                                Math.cos(dByR) - Math.sin(lat1) * Math.sin(lat)
                        );

        val location2 = Location("B")
        location2.longitude = lon
        location2.latitude = lat
        return location2
    }

    fun offset(distance: Double, bearing: Double): Location {
        val bearing1 = toRadians(bearing)
        val lat1 = toRadians(this.latitude)
        val lon1 = toRadians(this.longitude);
        val dByR = distance / RADIUS;
        val lat = asin(
                sin(lat1) * cos(dByR) + cos(lat1) * sin(dByR) * cos(bearing1)
        );
        val lon =
                lon1 +
                        atan2(
                                sin(bearing1) * sin(dByR) * cos(lat1),
                                cos(dByR) - sin(lat1) * sin(lat)
                        );

        val location2 = Location("B")
        location2.longitude = toDegrees(lon)
        location2.latitude = toDegrees(lat)
        return location2
    }

    fun bearingNormalize(bearing: Double): Double {
        var res: Double = bearing + 360 * 2
        res %= 360
        return res
    }

    private companion object {
        const val RADIUS = 6378137.0
    }
}