package com.atlas.mars.glidecon

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.atlas.mars.glidecon.database.MapDateBase
import com.atlas.mars.glidecon.dialog.DialogInfoPermission
import com.atlas.mars.glidecon.dialog.DialogLendingBox
import com.atlas.mars.glidecon.dialog.DialogStartAltitude
import com.atlas.mars.glidecon.dialog.DialogWindSetting
import com.atlas.mars.glidecon.fragment.*
import com.atlas.mars.glidecon.model.MapBoxModel
import com.atlas.mars.glidecon.model.LandingBoxViewModel
import com.atlas.mars.glidecon.service.LocationService
import com.atlas.mars.glidecon.store.MapBoxStore
import com.google.android.material.navigation.NavigationView
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

interface Ololo {
    val ll: FragmentActivity
}


class MapBoxActivity : AppCompatActivity(), Ololo {
    private val TAG = "MapBoxActivity"
    private var mapView: MapView? = null
    private var bound: Boolean = false;
    private var locationService: LocationService? = null
    lateinit var serviceIntent: Intent
    lateinit var toolbar: Toolbar
    lateinit var mapBoxModel: MapBoxModel
    var isSubscribed = false;
    lateinit var mapDateBase: MapDateBase
    // lateinit var mapBoxStore: MapBoxStore
    private var fragmentTrackBuild: FragmentTrackBuild? = null
    private var fragmentDashboard: FragmentDashboard? = null
    private val _onDestroy = AsyncSubject.create<Boolean>();

    var dialogLendingBox: AlertDialog? = null

    private val screenWidth: Int
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = this.windowManager.currentWindowMetrics
                val insets: Insets = windowMetrics.windowInsets
                        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                windowMetrics.bounds.width() - insets.left - insets.right
            } else {
                val displayMetrics = DisplayMetrics()
                this.windowManager.defaultDisplay.getMetrics(displayMetrics)
                displayMetrics.widthPixels
            }
        }
    override val ll: FragmentActivity
        get() {
            return this
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isSubscribed = true
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        serviceIntent = Intent(this, LocationService::class.java)
        MapBoxStore.onCreate()
        mapDateBase = MapDateBase(this)
        mapDateBase.initValues()

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_mapbox)

        setupStoreSubscribers()
        setupDrawerLayout()
        setupGpsStatusFrame()
        setupCompassFrame()
        setupFollowFrame()
        setupTiltLayout()
        setupZoomControl()
        setupWindLayout()
        screenWidth

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState ?: Bundle())
        val myViewModel = ViewModelProviders.of(this).get(LandingBoxViewModel::class.java)
        mapBoxModel = MapBoxModel(mapView!!, this as Context, myViewModel)
        myViewModel.startLatLng.observe(this, {
            if (it != null && dialogLendingBox?.isShowing == false) {
                dialogLendingBox?.show()
            }
        })
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

    private fun setupWindLayout() {
        val fm = this.supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        ft.add(R.id.wind_layout, FragmentWind())
        ft.commit()
    }

    private fun setupZoomControl() {
        val fm = this.supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        ft.add(R.id.zoom_layout, FragmentZoomControl())
        ft.commit()
    }

    private fun setupTiltLayout() {
        val fm = this.supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        ft.add(R.id.map_tilt, FragmentTilt())
        ft.commit()
    }

    private fun setupDrawerLayout() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_open)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(fun(menuItem: MenuItem): Boolean {
            drawer.closeDrawer(GravityCompat.START);
            MapBoxStore.routeBuildProgress.onNext(false)
            when (menuItem.itemId) {
                R.id.wind_menu_item -> {
                    val dialogWindSetting = DialogWindSetting(this)
                    dialogWindSetting.create().show()
                    // showWindowSettingFrame()
                }
                R.id.start_altitude -> {
                    DialogStartAltitude(this).create().show()
                }
                R.id.rectangular_landing_pattern -> {

                    if (dialogLendingBox == null) {
                        dialogLendingBox = DialogLendingBox(this, this).create()
                    }

                    dialogLendingBox?.show()
                }
                R.id.list_saved_track -> {
                    val intent = Intent(this, ListSavedTrack::class.java)
                    startActivityForResult(intent, LIST_SAVED_TRACK_CODE)
                    Log.d(TAG, "list saved track clicked")
                }
                R.id.track_build -> {
                    MapBoxStore.routeBuildProgress.onNext(true)
                }
            }
            return true
        })

    }

    private fun setupStoreSubscribers() {
        MapBoxStore.routeBuildProgress
                .takeUntil(_onDestroy)
                .distinctUntilChanged()
                .subscribeBy {
                    if (it) {
                        showBuildTrackFrame()
                        hideBikeComputerFrame()
                    } else {
                        hideBuildTrackFrame()
                        showBikeComputerFrame()
                    }
                }
    }

    private fun showBuildTrackFrame() {
        val fm = this.supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        fragmentTrackBuild = FragmentTrackBuild()
        fragmentTrackBuild?.let { fr -> ft.add(R.id.track_build_layout, fr).commit() }

    }

    private fun hideBuildTrackFrame() {
        fragmentTrackBuild?.let { fr ->
            this.supportFragmentManager.beginTransaction().remove(fr).commit(); fragmentTrackBuild = null
        }
    }

    private fun setupGpsStatusFrame() {
        val fm = this.supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        //ft.replace(R.id.gpsStatusFrameView, FragmentGpsStatus());
        ft.add(R.id.gpsStatusFrameView, FragmentGpsStatus());
        ft.commit();
//        val fm: FragmentManager = fragmentManager
        // FragmentActivity
        /* FragmentActivity.getSupportFragmentManager()
        val fTrans = getFragmentManager().beginTransaction();*/
        /* val gpsStatus = FragmentGpsStatus()
         val gpsStatusTrans = fragmentManager.beginTransaction()
         gpsStatusTrans.add(R.id.gpsStatusFrameView, gpsStatus)
         gpsStatusTrans.commit()*/
    }

    private fun showBikeComputerFrame(){
        val fm = this.supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        fragmentDashboard = FragmentDashboard()
        fragmentDashboard?.let { fr ->  ft.add(R.id.bike_computer_layout, fr).commit()}

    }

    private fun hideBikeComputerFrame(){
        fragmentDashboard?.let { fr -> this.supportFragmentManager.beginTransaction().remove(fr).commit() }
    }



    private fun setupCompassFrame() {
        val fm = this.supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        ft.add(R.id.compass_layout, FragmentCompass())
        ft.commit();
    }

    private fun setupFollowFrame() {
        val fm = this.supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        ft.add(R.id.follow_layout, FragmentFollow())
        ft.commit();
    }

    private fun showWindowSettingFrame() {
        val fm = this.supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        ft.add(R.id.wind_setting_layout, FragmentWindSetting())
        ft.commit();
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_mapbox_main, menu)
        //val actionBar: ActionBar? = supportActionBar as ActionBar?
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_calc -> {
                Log.d(TAG, "action_settings")
                val questionIntent = Intent(this, MainActivity::class.java)
                startActivityForResult(questionIntent, LIST_SAVED_TRACK_CODE)
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ),
                1
        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray,
    ) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startBackgroundProcess()
        }
    }

    private fun startBackgroundProcess() {
        bindService(
                serviceIntent,
                sCon,
                Context.BIND_AUTO_CREATE
        )
        startService(serviceIntent)
    }

    override fun onResume() {

        super.onResume()

        if (mapDateBase.getAgreement()) {
            checkPermissionAndStart()
        } else {
            val dialogWindSetting = DialogInfoPermission(this)
            dialogWindSetting.create().show()
            dialogWindSetting.onAgreeSubject
                    .subscribeBy {
                        if (it) {
                            mapDateBase.saveAgreementAgree()
                            checkPermissionAndStart()
                        }
                    }
        }
    }

    private fun checkPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "no location permission")
            requestLocationPermission()
            return
        }
        startBackgroundProcess()
    }

    override fun onPause() {

        super.onPause()
        Log.d(TAG, "onPause")
        if (bound) {
            unbindService(sCon);
            stopService(serviceIntent)
        }
    }

    override fun onDestroy() {
        isSubscribed = false
        mapView?.onDestroy()
        mapBoxModel.onDestroy()
        mapDateBase.onUnsubscribe()
        MapBoxStore.onDestroy()
        this._onDestroy.onNext(true)
        this._onDestroy.onComplete()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LIST_SAVED_TRACK_CODE -> {
                Log.d(TAG, "returned from list saved track")
            }
        }
    }

    companion object {
        val LIST_SAVED_TRACK_CODE = 50
    }
}