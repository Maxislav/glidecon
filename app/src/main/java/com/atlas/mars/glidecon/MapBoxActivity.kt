package com.atlas.mars.glidecon

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.atlas.mars.glidecon.service.LocationService
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.locationSubject
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.mapboxMapSubject
import com.google.android.material.navigation.NavigationView
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy


class MapBoxActivity : AppCompatActivity() {
    private val TAG = "MapBoxActivity"
    private var mapView: MapView? = null
    private var bound: Boolean = false;
    private var locationService: LocationService? = null
    lateinit var serviceIntent: Intent
    lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        serviceIntent = Intent(this, LocationService::class.java)


        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_mapbox)

        setupDrawerLayout()


        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->
            mapboxMapSubject.onNext(mapboxMap)
            mapboxMapSubject.onComplete()
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {

                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
            }
        }
        mapboxMapSubject.subscribeBy(
                onNext = { value: MapboxMap ->
                    Log.d(TAG, "mapboxMap defined")
                }
        )
        locationSubject.subscribeBy(
                onNext = { location: Location ->
                    Log.d(TAG, "location ${location.toString()}")
                }
        )
        val list = listOf(mapboxMapSubject, locationSubject)

        val ff = Observables.combineLatest(mapboxMapSubject, locationSubject)
                .subscribeBy(
                        onNext = { pair: Pair<MapboxMap, Location> ->


                        }
                )

    }

    val sCon = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.service
            locationService?.startLocationListener()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
            bound = false
        }

    }

    private fun setupDrawerLayout(){
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_open)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { itt -> false }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        //val actionBar: ActionBar? = supportActionBar as ActionBar?
        return true
    }

    override fun onResume() {
        super.onResume()
        bindService(
                serviceIntent,
                sCon,
                Context.BIND_AUTO_CREATE
        )
        startService(serviceIntent)

    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        if (bound) {
            unbindService(sCon);
            stopService(serviceIntent)
        }
    }


}