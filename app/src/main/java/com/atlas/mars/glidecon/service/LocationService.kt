package com.atlas.mars.glidecon.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.locationSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.satelliteSubject
import com.atlas.mars.glidecon.util.LocationUtil
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit


class LocationService : Service() {
    private val TAG = "LocationService"
    private val localBinder = LocalBinder()
    private var locationManagerGps: LocationManager? = null
    private lateinit var gpsListener: GPSListener
    var mGnssStatusCallback: GnssStatus.Callback? = null
    var gpsStatusListener: GpsStatus.Listener? = null
    var isDebug = false

    override fun onBind(intent: Intent?): IBinder {
        return localBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        debug()
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
                200,
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
                    val sat: Map<MapBoxStore.SatCount, Int> = mapOf(MapBoxStore.SatCount.TOTAl to satellitesTotalCount, MapBoxStore.SatCount.USED  to usedSatellites)
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
                        satelliteSubject.onNext(mapOf(MapBoxStore.SatCount.TOTAl to satellitesTotalCount, MapBoxStore.SatCount.USED to usedSatellites))
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

    private fun debug(){
        isDebug = true
        val locationList = mutableListOf<Location>()
        var move = true

        var alt = 700.0
        for(a in 0..360 step 5){
            val locat = LocationUtil()
            locat.latitude =50.3988
            locat.longitude = 30.0672
            locat.time = (a*10000.0).toLong()

            val radLocation = locat.offset(400.toDouble(), a.toDouble())
          //  radLocation.altitude = (alt - a*0.3).toDouble()

            if(a<90){
              alt-=2
            }else if(a<120){
                alt +=2
            }else if(a < 180){
                alt+=2.5
            } else if(a<270) {
                alt +=1
            }
            radLocation.altitude = alt.toDouble()
            radLocation.time = (a*10000.0).toLong()
            locationList.add(radLocation)
        }

        val handler = object : Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            override fun handleMessage(msg: Message) {

                val location = msg.obj as Location
                locationSubject.onNext(location)
            }
        }

        Observable
                .interval(1, TimeUnit.SECONDS)
                .takeWhile{move}
                .subscribeBy {
                    if(0<locationList.size){
                        val currLocation = locationList.removeAt(0)
                        currLocation.time = System.currentTimeMillis()

                        val msg = handler.obtainMessage(1, currLocation)
                        handler.sendMessage(msg)

                    }else{
                        move = false
                    }


                }
    }

    inner class GPSListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            if(!isDebug){
                locationSubject.onNext(location)
            }

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