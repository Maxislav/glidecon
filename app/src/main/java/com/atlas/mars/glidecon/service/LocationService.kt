package com.atlas.mars.glidecon.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.locationSubject

class LocationService : Service() {
    private val TAG = "LocationService"
    private val localBinder = LocalBinder()
    private var locationManagerGps: LocationManager? = null
    private lateinit var gpsListener: GPSListener

    override fun onBind(intent: Intent?): IBinder {
        return localBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    inner class LocalBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        stopLocationListener()
        super.onDestroy()
    }

    fun startLocationListener() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }
        locationManagerGps = getSystemService(LOCATION_SERVICE) as LocationManager
        gpsListener = GPSListener()
        locationManagerGps?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                0F,
                gpsListener

        )
    }

    private fun stopLocationListener() {
        locationManagerGps?.removeUpdates(gpsListener)
    }

    inner class GPSListener : LocationListener{
        override fun onLocationChanged(location: Location) {
            locationSubject.onNext(location)
        }
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    companion object {
        private const val PACKAGE_NAME = "com.atlas.mars.glidecon.service"
        const val INTENT_NAME = "${this.PACKAGE_NAME}.location.service"
    }
}