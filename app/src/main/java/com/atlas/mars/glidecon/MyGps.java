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
import java.util.HashMap;

/**
 * Created by Администратор on 7/24/15.
 */
public class MyGps implements LocationListener, GpsStatus.Listener {
    MyService service;
    final String TAG = "MyGpsLog";
    Location location;
    Date date;
    HashMap<String, String> mapSetting;
    double startAltitude = 0.0;
    double K = 0.0;
    double alt;
    double vario;
    double _vario = 0.0;
    double quality = 0.0;

    MyGps(MyService service) {
        this.service = service;
        mapSetting = DataBaseHelper.mapSetting;
    }


    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double altitude =  location.getAltitude();
        double speed = 0.0;
        long time;
        float distance;



        if (this.location != null) {
            distance = location.distanceTo(this.location);
            time = (new Date().getTime()) - date.getTime();
            speed = getSpeed(distance, time);
            double dAlt = location.getAltitude() - this.alt;
            vario = getVario(dAlt, time);
            quality = getQuality(dAlt, distance);
        }
        if(mapSetting.get(DataBaseHelper.START_ALTITUDE)!=null){
            try {
                startAltitude =  Integer.parseInt(mapSetting.get(DataBaseHelper.START_ALTITUDE));
                            }catch (Exception e){
                Log.d(TAG, e.toString(), e);
            }
        }
        altitude = altitude - startAltitude;
        altitude = round(altitude, 1);
        Intent intent = new Intent();
        intent.setAction(MyService.LOCATION);
        intent.putExtra("lat", lat);
        intent.putExtra("speed", speed);
        intent.putExtra("altitude", altitude);
        intent.putExtra("vario", vario);
        intent.putExtra("quality", quality);
        service.sendBroadcast(intent);
        this.location = location;
        this.alt =  location.getAltitude();
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

    private  double getVario(double dAlt, long time){
        double t = (double) time;
        if (t != 0.0) {
            t = t / 1000;
            _vario = dAlt/t;
            _vario = round(_vario, 1);

        }
        return  _vario;
    }

    private  double getQuality(double h, double distance){
        double k;
        double _d = 0.0;
        if(h<distance){
           _d = Math.sqrt(Math.pow(distance, 2) -Math.pow(h, 2));
        }
        k= _d/h;
        k = round(k,2);
        return k;
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
        double b =  0.0;
        if(d!=0.0 && d !=Double.POSITIVE_INFINITY && d !=Double.NEGATIVE_INFINITY ){
            Log.d(TAG, d+"  " +prec);
            try {
                b = new BigDecimal(d).setScale(prec, RoundingMode.UP).doubleValue();
            }catch (Exception e){
                Log.e(TAG, e.toString(), e);
                Log.e(TAG, d+"  " +prec);
            }

        }
        return b;
    }
}
