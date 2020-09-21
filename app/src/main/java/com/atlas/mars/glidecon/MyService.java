package com.atlas.mars.glidecon;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.HashMap;

/**
 * Created by Администратор on 7/24/15.
 */
public class MyService extends Service {
    final static String LOCATION = "LOCATION";
    final String LOG_TAG = "MyServiceLog";
    public LocationManager locationManagerGps;
    public LocationListener locationListenerGps;
    MyThread thread;
    HashMap<String, String> mapSetting;

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        locationManagerGps = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapSetting = new DataBaseHelper(this).mapSetting;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        locationManagerGps.removeUpdates(locationListenerGps);
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    void someTask() {
        locationListenerGps = new MyGps(this);
        int INTERVAL_UPDATE = 1000;
        if (mapSetting.get(DataBaseHelper.INTERVAL_UPDATE) != null) {
            try {
                INTERVAL_UPDATE = Integer.parseInt(mapSetting.get(DataBaseHelper.INTERVAL_UPDATE));
            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString(), e);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            return;
        }
        locationManagerGps.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL_UPDATE, 10, locationListenerGps);
       /* thread = new MyThread();
        thread.start();*/
    }

    class MyThread extends Thread{
        MyThread(){
            super();
        }

        @Override
        public void run() {
            super.run();
            //locationListenerGps = new MyGps();
            //locationManagerGps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListenerGps);
        }
    }
}
