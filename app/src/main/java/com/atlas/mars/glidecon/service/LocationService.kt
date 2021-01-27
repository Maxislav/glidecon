package com.atlas.mars.glidecon.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.locationSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.satelliteSubject

class LocationService : Service() {
    private val TAG = "LocationService"
    private val localBinder = LocalBinder()
    private var locationManagerGps: LocationManager? = null
    private lateinit var gpsListener: GPSListener
    var mGnssStatusCallback: GnssStatus.Callback? = null
    var gpsStatusListener: GpsStatus.Listener? = null

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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mGnssStatusCallback = object : GnssStatus.Callback() {
                override fun onSatelliteStatusChanged(status: GnssStatus){
                    if (Companion.isNotPermission(this@LocationService)) {
                        return
                    }
                    val satellitesTotalCount: Int = status.satelliteCount
                    var usedSatellites: Int = 0
                    for (i in 0 until satellitesTotalCount) {
                        if (status.usedInFix(i)) {
                            usedSatellites++
                        }
                    }
                    Log.d(TAG, "total ${satellitesTotalCount}, used ${usedSatellites}")
                    val sat: Map<String, Int> = mapOf("total" to satellitesTotalCount, "used" to usedSatellites)
                    satelliteSubject.onNext(sat)
                }
            }
            locationManagerGps?.registerGnssStatusCallback(mGnssStatusCallback!!, null)
        } else {
            gpsStatusListener = object : GpsStatus.Listener{
                @SuppressLint("MissingPermission")
                override fun onGpsStatusChanged(event: Int) {
                    if (isNotPermission(this@LocationService)) {
                        return
                    }
                    val gpsStatus: GpsStatus? = locationManagerGps!!.getGpsStatus(null)
                    if(gpsStatus!=null){
                        val satellites = gpsStatus.satellites
                        val sats: Iterator<GpsSatellite> = satellites.iterator()
                        var satellitesTotalCount = 0
                        var usedSatellites = 0
                        while (sats.hasNext()){
                            satellitesTotalCount++
                            val satellite = sats.next()
                            if(satellite.usedInFix()){
                                usedSatellites++
                            }
                        }
                        satelliteSubject.onNext(mapOf("total" to satellitesTotalCount, "used" to usedSatellites))
                    }
                }
            }
            locationManagerGps!!.addGpsStatusListener(gpsStatusListener)
        }
    }

    private fun stopLocationListener() {
        locationManagerGps?.removeUpdates(gpsListener)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
            locationManagerGps?.unregisterGnssStatusCallback(mGnssStatusCallback!!)
        }else{
            locationManagerGps?.removeGpsStatusListener(gpsStatusListener)
        }

    }

    inner class GPSListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationSubject.onNext(location)
        }
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
      /*  override fun onGpsStatusChanged(eventt: Int){

        }*/
    }

    companion object {
        private const val PACKAGE_NAME = "com.atlas.mars.glidecon.service"
        const val INTENT_NAME = "${this.PACKAGE_NAME}.location.service"
        fun isNotPermission(context: Context): Boolean {
            return  (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

        }
    }
}