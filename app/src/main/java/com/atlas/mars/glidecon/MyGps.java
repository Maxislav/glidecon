package com.atlas.mars.glidecon;

import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Created by Администратор on 7/24/15.
 */
public class MyGps implements LocationListener, GpsStatus.Listener {
    MyService service;
    final String TAG = "MyGpsLog";
    Location location;
    Date date;

    MyGps(MyService service) {
        this.service = service;
        //   activity =

    }


    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //location.ge
        double lat = location.getLatitude();
        double altitude =  location.getAltitude();
        double speed = 0.0;
        long time;
        float distance = 0.0f;
        if (this.location == null) {

        } else {
            distance = location.distanceTo(this.location);
            time = (new Date().getTime()) - date.getTime();
            speed = getSpeed(distance, time);
        }

        Intent intent = new Intent();
        intent.setAction(MyService.LOCATION);
        intent.putExtra("lat", lat);
        intent.putExtra("speed", speed);
        intent.putExtra("altitude", altitude);
        service.sendBroadcast(intent);
        this.location = location;
        date = new Date();
    }

    double sp = 0.0;

    private double getSpeed(float dist, long time) {

        double t = (double) time;
        if (t != 0.0) {
            t = t / 1000;
            if (3.6 * dist / (t) != 0.0) {
                sp = 3.6 * dist / (t);
                sp = round(sp, 1);
            }
        }
        return sp;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private double round(double d, int prec) {
        return new BigDecimal(d).setScale(prec, RoundingMode.UP).doubleValue();
    }
}
