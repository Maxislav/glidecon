package com.atlas.mars.glidecon.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng

class MapDateBase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "mapSettingDataBase"
        private const val TABLE_SETTING_MAP = "settingMap"
        private const val TABLE_SETTING_PARAM = "settingFlyParam"

        private const val UID = "id"
        private const val KEY = "param"
        private const val VALUE = "value"
    }

    init {

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableSettingMap = "CREATE TABLE if not exists $TABLE_SETTING_MAP (" +
                "$UID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lat DOUBLE, " +
                "lon DOUBLE, " +
                "zoom DOUBLE, " +
                "bearing DOUBLE, " +
                "tilt DOUBLE" +
                ");"
        db?.execSQL(createTableSettingMap)

        val createTableFlyParams = "CREATE TABLE if not exists $TABLE_SETTING_PARAM ($UID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY VARCHAR(255), $VALUE  VARCHAR(255) );"
        db?.execSQL(createTableFlyParams)
        // createDefaultMap()

        /*"CREATE TABLE if not exists "
            +TABLE_SETTING+" (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            +KEY +" VARCHAR(255), " +  VALUE +  " VARCHAR(255) " +");"*/
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //
    }

    fun saveCameraPosition(cameraPosition: CameraPosition) {
        cameraPosition.bearing
        cameraPosition.tilt
        cameraPosition.target
        cameraPosition.zoom
        val sdb = readableDatabase
        val cv = ContentValues()



        cv.put("lat", cameraPosition.target.latitude)
        cv.put("lon", cameraPosition.target.longitude)
        cv.put("bearing", cameraPosition.bearing)
        cv.put("tilt", cameraPosition.tilt)
        cv.put("zoom", cameraPosition.zoom)

        val jquery = "SELECT * FROM $TABLE_SETTING_MAP"
        val cursor: Cursor = sdb.rawQuery(jquery, null)
        if (0 < cursor.count) {
            cursor.moveToFirst()
            val id = cursor.getString(cursor.getColumnIndex("id"))
            sdb.update(TABLE_SETTING_MAP, cv, "id=?", arrayOf(id))
        } else {
            sdb.insert(TABLE_SETTING_MAP, null, cv)
        }
        cursor.close()
        sdb.close()
    }

    fun getCameraPosition(): CameraPosition? {


        val jquery = "SELECT * FROM $TABLE_SETTING_MAP"
        val sdb = readableDatabase
        val cursor: Cursor = sdb.rawQuery(jquery, null)
        if (0 < cursor.count) {
            cursor.moveToFirst()
            val lat  = cursor.getDouble(cursor.getColumnIndex("lat"))
            val lon  = cursor.getDouble(cursor.getColumnIndex("lon"))
            val bearing  = cursor.getDouble(cursor.getColumnIndex("bearing"))
            val tilt  = cursor.getDouble(cursor.getColumnIndex("tilt"))
            val zoom  = cursor.getDouble(cursor.getColumnIndex("zoom"))
            return CameraPosition.Builder()
                    .bearing(bearing)
                    .target(LatLng(lat, lon))
                    .tilt(tilt)
                    .zoom(zoom)
                    .build()
        }

        cursor.close()
        sdb.close()
        return null
    }

}