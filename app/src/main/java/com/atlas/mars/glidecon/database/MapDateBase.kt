package com.atlas.mars.glidecon.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.windSubject
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import io.reactivex.rxkotlin.subscribeBy

class MapDateBase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private var isSubscribed = true

    companion object {
        private const val TAG = "MapDateBase"

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "mapSettingDataBase"
        private const val TABLE_SETTING_MAP = "settingMap"
        private const val TABLE_SETTING_PARAM = "settingFlyParam"

        private const val UID = "id"
        private const val NAME = "name"
        private const val VALUE = "value"

        private const val WIND_DIRECTION = "windDirection"
        private const val WIND_SPEED = "windSpeed"
        private const val START_ALTITUDE = "startAltitude"
        private const val LIFT_TO_DRAG_RATIO = "liftToDragRatio"
        private const val OPTIMAL_SPEED = "optimalSpeed"

    }

    init {

    }

    fun initValues() {
        val query = "SELECT * FROM $TABLE_SETTING_PARAM WHERE $NAME=?"
        val sdb = readableDatabase
        var skip: Long = 0
        var cursor: Cursor = sdb.rawQuery(query, arrayOf(WIND_DIRECTION))
        var windDirection: Double? = null
        var windSpeed: Double? = null
        var startAltitude: Double? = null
        if (0 < cursor.count) {
            cursor.moveToFirst()
            windDirection = cursor.getDouble(cursor.getColumnIndex(VALUE))
        }
        cursor = sdb.rawQuery(query, arrayOf(WIND_SPEED))
        if (0 < cursor.count) {
            cursor.moveToFirst()
            windSpeed = cursor.getDouble(cursor.getColumnIndex(VALUE))
        }
        skip = 0
        if (windDirection != null && windSpeed != null) {
            skip=1
            windSubject.onNext(mapOf(MapBoxStore.Wind.SPEED to windSpeed, MapBoxStore.Wind.DIRECTION to windDirection))
        }
        windSubject
                .takeWhile { isSubscribed }
                .skip(skip)
                .subscribeBy(
                        onNext = {
                            saveWindParams(it)
                        }
                )


        /** START_ALTITUDE **/
        skip = 0
        cursor = sdb.rawQuery(query, arrayOf(START_ALTITUDE))
        if (0 < cursor.count) {
            cursor.moveToFirst()
            startAltitude = cursor.getDouble(cursor.getColumnIndex(VALUE))
            MapBoxStore.startAltitudeSubject.onNext(startAltitude)
            skip = 1
        }
        MapBoxStore.startAltitudeSubject.takeWhile { isSubscribed }
                .skip(skip)
                .subscribeBy(
                        onNext = {
                            saveParam(START_ALTITUDE, it)
                        }
                )
        /** LIFT_TO_DRAG_RATIO **/
        cursor = sdb.rawQuery(query, arrayOf(LIFT_TO_DRAG_RATIO))
        skip = 0
        if (0 < cursor.count) {
            cursor.moveToFirst()
            val ratio = cursor.getDouble(cursor.getColumnIndex(VALUE))
            MapBoxStore.liftToDragRatioSubject.onNext(ratio)
            skip = 1
        }

        MapBoxStore.liftToDragRatioSubject.takeWhile { isSubscribed }
                .skip(skip)
                .subscribeBy(
                        onNext = {
                            saveParam(LIFT_TO_DRAG_RATIO, it)
                        }
                )

        /** OPTIMAL_SPEED **/
        cursor = sdb.rawQuery(query, arrayOf(OPTIMAL_SPEED))
        skip = 0
        if (0 < cursor.count) {
            cursor.moveToFirst()
            val speed = cursor.getDouble(cursor.getColumnIndex(VALUE))
            MapBoxStore.optimalSpeedSubject.onNext(speed)
            skip = 1
        }
        MapBoxStore.optimalSpeedSubject.takeWhile { isSubscribed }
                .skip(skip)
                .subscribeBy(
                        onNext = {
                            saveParam(OPTIMAL_SPEED, it)
                        }
                )

        cursor.close()
        //sdb.close()
    }

    private fun saveParam(name: String, value: Double) {
        val sdb = readableDatabase
        val id = isValueExist(name, sdb)
        val cv = ContentValues()
        cv.put(NAME, name)
        cv.put(VALUE, value)
        if (-1 < id) {
            sdb.update(TABLE_SETTING_PARAM, cv, "$UID=?", arrayOf(id.toString()))
        } else {
            sdb.insert(TABLE_SETTING_PARAM, null, cv)
        }

        //sdb.close()
    }

    private fun saveWindParams(windSetting: Map<MapBoxStore.Wind, Double>) {
        val sdb = readableDatabase
        val cv = ContentValues()
        cv.put(NAME, WIND_SPEED)
        cv.put(VALUE, windSetting[MapBoxStore.Wind.SPEED])
        var id = isValueExist(WIND_SPEED, sdb)
        if (-1 < id) {
            sdb.update(TABLE_SETTING_PARAM, cv, "$UID=?", arrayOf(id.toString()))
        } else {
            sdb.insert(TABLE_SETTING_PARAM, null, cv)
        }
        cv.clear()

        cv.put(NAME, WIND_DIRECTION)
        cv.put(VALUE, windSetting[MapBoxStore.Wind.DIRECTION])
        id = isValueExist(WIND_DIRECTION, sdb)
        if (-1 < id) {
            sdb.update(TABLE_SETTING_PARAM, cv, "$UID=?", arrayOf(id.toString()))
        } else {
            sdb.insert(TABLE_SETTING_PARAM, null, cv)
        }
        //sdb.close()
    }

    @SuppressLint("Recycle")
    private fun isValueExist(value: String, sdb: SQLiteDatabase): Int {
        val jquery = "SELECT * FROM $TABLE_SETTING_PARAM WHERE $NAME=?"
        val cursor: Cursor = sdb.rawQuery(jquery, arrayOf(value))
        var id = -1
        if (0 < cursor.count) {
            cursor.moveToFirst()
            id = cursor.getInt(cursor.getColumnIndex(UID))
        }
        return id
    }


    fun onUnsubscribe() {
        isSubscribed = false
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

        val createTableFlyParams = "CREATE TABLE if not exists $TABLE_SETTING_PARAM ($UID INTEGER PRIMARY KEY AUTOINCREMENT, $NAME VARCHAR(255), $VALUE  DOUBLE );"
        db?.execSQL(createTableFlyParams)
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
            val lat = cursor.getDouble(cursor.getColumnIndex("lat"))
            val lon = cursor.getDouble(cursor.getColumnIndex("lon"))
            val bearing = cursor.getDouble(cursor.getColumnIndex("bearing"))
            val tilt = cursor.getDouble(cursor.getColumnIndex("tilt"))
            val zoom = cursor.getDouble(cursor.getColumnIndex("zoom"))
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