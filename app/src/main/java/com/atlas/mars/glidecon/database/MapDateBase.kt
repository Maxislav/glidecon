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

        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "mapSettingDataBase"
        private const val TABLE_SETTING_MAP = "settingMap"
        private const val TABLE_SETTING_PARAM = "settingFlyParam"

        private const val TABLE_ROUTE_NAME = "routeName"
        private const val TABLE_ROUTE_POINT = "routePoint"

        private const val UID = "id"
        private const val NAME = "name"
        private const val VALUE = "value"
        private const val NAME_ID = "nameId"

        private const val DISTANCE = "distance"
        private const val DATE_TIME = "dateTime"
        private const val ACTIVE = "active"
        private const val PROPS = "props"

        /**
         * default | turning
         */
        private const val POINT_TYPE = "pointType"

        private const val WIND_DIRECTION = "windDirection"
        private const val WIND_SPEED = "windSpeed"
        private const val START_ALTITUDE = "startAltitude"
        private const val LIFT_TO_DRAG_RATIO = "liftToDragRatio"
        private const val OPTIMAL_SPEED = "optimalSpeed"
        private const val LANDING_BOX_ANGLE = "landingBoxAngle"
        private const val LANDING_START_POINT_LNG = "landingStartPointLng"
        private const val LANDING_START_POINT_LAT = "landingStartPointLat"
        private const val LANDING_RATIO_FLY = "landingRatioFly"
        private const val LANDING_RATIO_FINAL = "landingRatioFinal"
        private const val ROUTE_TYPE = "routeType"
        private const val ROUTE_ID = "routeId"


        private const val AGREEMENT_AGREE = "agreementAgree"


    }

    init {

    }

    override fun onCreate(db: SQLiteDatabase?) {
        createTableSetting(db)
        createTableRoutes(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createTableRoutes(db)
        }
    }

    private fun createTableSetting(db: SQLiteDatabase?) {
        val createTableSettingMap = "CREATE TABLE if not exists $TABLE_SETTING_MAP" +
                " (" +
                "$UID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lat DOUBLE, " +
                "lon DOUBLE, " +
                "zoom DOUBLE, " +
                "bearing DOUBLE, " +
                "tilt DOUBLE" +
                ");"
        db?.execSQL(createTableSettingMap)

        val createTableFlyParams = "CREATE TABLE if not exists $TABLE_SETTING_PARAM" +
                " (" +
                " $UID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $NAME VARCHAR(255)," +
                " $VALUE DOUBLE" +
                ");"
        db?.execSQL(createTableFlyParams)

    }

    private fun createTableRoutes(db: SQLiteDatabase?) {
        val createTRouteName = "CREATE TABLE if not exists $TABLE_ROUTE_NAME" +
                " (" +
                " $UID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $NAME VARCHAR(255)," +
                " $DISTANCE DOUBLE," +
                " $DATE_TIME TEXT," +
                " $ACTIVE BOOLEAN NOT NULL DEFAULT 0," +
                " $PROPS VARCHAR(255) DEFAULT NULL" +
                ");"
        db?.execSQL(createTRouteName)
        /**
         * type
         */

        val createTRouePoint = "CREATE TABLE if not exists $TABLE_ROUTE_POINT" +
                " (" +
                " $UID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $NAME_ID INTEGER," +
                " lat DOUBLE," +
                " lon DOUBLE," +
                " alt DOUBLE," +
                " $POINT_TYPE VARCHAR(12) DEFAULT `point`," +
                " marker VARCHAR" +
                ");"
        db?.execSQL(createTRouePoint)
        Log.d(TAG, "createTableRoutes complete")
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
            skip = 1
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
        /** STARTING POINT DIRECTION **/
        cursor = sdb.rawQuery(query, arrayOf(LANDING_BOX_ANGLE))
        skip = 0
        if (0 < cursor.count) {
            cursor.moveToFirst()
            val angle = cursor.getDouble(cursor.getColumnIndex(VALUE))
            MapBoxStore.landingBoxAngleSubject.onNext(angle)
            skip = 1
        }
        MapBoxStore.landingBoxAngleSubject.takeWhile { isSubscribed }
                .skip(skip)
                .subscribeBy(
                        onNext = {
                            saveParam(LANDING_BOX_ANGLE, it)
                        }
                )

        /** STARl PoINT LAT LNG**/
        cursor = sdb.rawQuery(query, arrayOf(LANDING_START_POINT_LAT))
        skip = 0
        var startLat: Double? = null
        var startLng: Double? = null
        if (0 < cursor.count) {
            cursor.moveToFirst()
            startLat = cursor.getDouble(cursor.getColumnIndex(VALUE))
        }

        cursor = sdb.rawQuery(query, arrayOf(LANDING_START_POINT_LNG))
        if (0 < cursor.count) {
            cursor.moveToFirst()
            startLng = cursor.getDouble(cursor.getColumnIndex(VALUE))
        }
        if (startLat != null && startLng != null) {
            skip = 1
            val lngLat = LatLng(startLat, startLng)
            MapBoxStore.landingStartPointSubject.onNext(lngLat)
        }

        MapBoxStore.landingStartPointSubject.takeWhile { isSubscribed }
                .skip(skip)
                .subscribeBy(
                        onNext = {
                            saveParam(LANDING_START_POINT_LAT, it.latitude)
                            saveParam(LANDING_START_POINT_LNG, it.longitude)
                        }
                )
        /** landing ratio */
        cursor = sdb.rawQuery(query, arrayOf(LANDING_RATIO_FLY))
        skip = 0
        var landingRatioFly: Double? = null
        var landingRatioFinal: Double? = null
        if (0 < cursor.count) {
            cursor.moveToFirst()
            landingRatioFly = cursor.getDouble(cursor.getColumnIndex(VALUE))
        }
        cursor = sdb.rawQuery(query, arrayOf(LANDING_RATIO_FINAL))
        if (0 < cursor.count) {
            cursor.moveToFirst()
            landingRatioFinal = cursor.getDouble(cursor.getColumnIndex(VALUE))
        }
        if (landingRatioFly != null && landingRatioFinal != null) {
            MapBoxStore.landingLiftToDragRatioSubject.onNext(
                    mapOf(
                            MapBoxStore.LandingLiftToDragRatio.FLY to landingRatioFly,
                            MapBoxStore.LandingLiftToDragRatio.FINAL to landingRatioFinal
                    )
            )
            skip = 1
        }
        MapBoxStore.landingLiftToDragRatioSubject.takeWhile { isSubscribed }
                .skip(skip)
                .subscribeBy {
                    it[MapBoxStore.LandingLiftToDragRatio.FLY]?.let { ratio -> saveParam(LANDING_RATIO_FLY, ratio) }
                    it[MapBoxStore.LandingLiftToDragRatio.FINAL]?.let { ratio -> saveParam(LANDING_RATIO_FINAL, ratio) }
                }

        /**
         * Route type
         */
        var initRouteType = false;
        cursor = sdb.rawQuery(query, arrayOf(ROUTE_TYPE))
        if (0 < cursor.count) {
            initRouteType = true
            cursor.moveToFirst()
            val routeType = cursor.getDouble(cursor.getColumnIndex(VALUE))
            val rr: MapBoxStore.RouteType = MapBoxStore.RouteType.from(routeType.toInt())
            MapBoxStore.routeType.onNext(rr)
            initRouteType = false
        } else {
            val rr: MapBoxStore.RouteType = MapBoxStore.RouteType.CAR
            MapBoxStore.routeType.onNext(rr)
        }
        MapBoxStore.routeType.takeWhile { isSubscribed }
                .filter { !initRouteType }
                .subscribeBy {
                    saveParam(ROUTE_TYPE, it.routeType.toDouble())
                }

        /**
         * Route selected
         */
        var initRouteId = false;
        cursor = sdb.rawQuery(query, arrayOf(ROUTE_ID))
        if (0 < cursor.count) {
            initRouteId = true
            cursor.moveToFirst()
            val routeID = cursor.getDouble(cursor.getColumnIndex(VALUE))
            MapBoxStore.activeRoute.onNext(routeID)
            initRouteId = false
        }
        MapBoxStore.activeRoute.takeWhile { isSubscribed }
                .filter { !initRouteId }
                .subscribeBy{
                    saveParam(ROUTE_ID, it)
                }

        cursor.close()
        //sdb.close()
    }

    fun getAgreement(): Boolean {
        val query = "SELECT * FROM $TABLE_SETTING_PARAM WHERE $NAME=?";
        val sdb = readableDatabase
        val cursor: Cursor = sdb.rawQuery(query, arrayOf(AGREEMENT_AGREE))
        if (cursor.count == 0) {
            return false
        }

        cursor.moveToFirst()
        return 0 < cursor.getDouble(cursor.getColumnIndex(VALUE))
    }

    fun saveAgreementAgree() {
        saveParam(AGREEMENT_AGREE, 1.0)
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