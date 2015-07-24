package com.atlas.mars.glidecon;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Администратор on 7/24/15.
 */
public class MyService extends Service {
    final static String LOCATION = "LOCATION";
    final String LOG_TAG = "MyServiceLog";
    public LocationManager locationManagerGps;
    public LocationListener locationListenerGps;
    MyThread thread;
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        locationManagerGps = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
        locationManagerGps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListenerGps);
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
