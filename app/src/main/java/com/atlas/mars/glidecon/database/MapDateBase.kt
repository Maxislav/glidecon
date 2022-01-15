package com.atlas.mars.glidecon.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.atlas.mars.glidecon.model.ListTrackItem
import com.atlas.mars.glidecon.model.RoutePoints
import com.atlas.mars.glidecon.model.TrackPoint
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.windSubject
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.AsyncSubject
import java.text.SimpleDateFormat
import java.util.*

class MapDateBase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

   //  private var isSubscribed = true
    private val _onDestroy = AsyncSubject.create<Boolean>();
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
        private const val TRACK_ID = "trackId"

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
                " $TRACK_ID INTEGER," +
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
                .takeUntil(_onDestroy)
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
        MapBoxStore.startAltitudeSubject.takeUntil(_onDestroy)
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
        MapBoxStore.liftToDragRatioSubject.takeUntil(_onDestroy)
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
        MapBoxStore.optimalSpeedSubject.takeUntil(_onDestroy)
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
        MapBoxStore.landingBoxAngleSubject.takeUntil(_onDestroy)
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

        MapBoxStore.landingStartPointSubject.takeUntil(_onDestroy)
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
        MapBoxStore.landingLiftToDragRatioSubject.takeUntil(_onDestroy)
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

        } else {
            val rr: MapBoxStore.RouteType = MapBoxStore.RouteType.CAR
            MapBoxStore.routeType.onNext(rr)
        }
        MapBoxStore.routeType.takeUntil(_onDestroy)
                .filter { !initRouteType }
                .subscribeBy {
                    saveParam(ROUTE_TYPE, it.routeType.toDouble())
                }
        initRouteType = false
        /**
         * Route selected
         */
        var initRouteId = false;
        cursor = sdb.rawQuery(query, arrayOf(ROUTE_ID))
        if (0 < cursor.count) {
            initRouteId = true
            cursor.moveToFirst()
            val routeID = cursor.getDouble(cursor.getColumnIndex(VALUE))
            MapBoxStore.activeRoute.onNext(routeID.toInt())
        }
        MapBoxStore.activeRoute.takeUntil(_onDestroy)
                .filter { !initRouteId }
                .subscribeBy {
                    saveParam(ROUTE_ID, it.toDouble())
                }
        initRouteId = false

        MapBoxStore.activeRoute.takeUntil(_onDestroy)
                .subscribeBy {
                    val name = getActiveRouteName(it)
                    MapBoxStore.activeRouteName.onNext(name)
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
        val agreement = 0 < cursor.getDouble(cursor.getColumnIndex(VALUE))
        cursor.close()
        return agreement
    }

    fun saveAgreementAgree() {
        saveParam(AGREEMENT_AGREE, 1.0)
    }

    fun saveTrackName(trackName: String, dist: Double = 0.0): Long {
        val sdb = readableDatabase
        val cv = ContentValues()
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        val dateTime = df.format(Date())
        cv.put(NAME, trackName)
        cv.put("distance", dist)
        cv.put("dateTime", dateTime)
        cv.put("active", 0)
        return sdb.insert(TABLE_ROUTE_NAME, null, cv)
    }

    fun getTrackNameLis(): ArrayList<ListTrackItem> {
        val sdb = readableDatabase
        val jquery = "SELECT * FROM $TABLE_ROUTE_NAME;"
        val cursor: Cursor = sdb.rawQuery(jquery, arrayOf())

        val trackList = arrayListOf<ListTrackItem>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(NAME))
            val trackId = cursor.getInt(cursor.getColumnIndex(UID))
            val distance = cursor.getDouble(cursor.getColumnIndex(DISTANCE))
            val dateTime = cursor.getString(cursor.getColumnIndex(DATE_TIME))
            val listTrackItem = ListTrackItem(trackId, name, distance, dateTime)
            trackList.add(listTrackItem)
        }
        cursor.close()
        return trackList;
    }

    private fun getActiveRouteName(id: Int): String {
        var name = ""
        val sdb = readableDatabase
        val jquery = "SELECT name FROM $TABLE_ROUTE_NAME WHERE $UID=?;"
        val cursor: Cursor = sdb.rawQuery(jquery, arrayOf(id.toString()))
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndex(NAME))
        }
        cursor.close()
        return name
    }

    fun saveTrackPoints(id: Long, m: MutableList<TrackPoint>) {
        val sdb = readableDatabase
        val cv = ContentValues()
        cv.put(TRACK_ID, id)
        m.forEach { trackPoint ->
            cv.put("lat", trackPoint.point.latitude)
            cv.put("lon", trackPoint.point.longitude)
            cv.put("alt", 0)
            cv.put("pointType", trackPoint.type.type)
            sdb.insert(TABLE_ROUTE_POINT, null, cv)
        }

    }

    @Synchronized
    fun getRoutePoints(id: Number): MutableList<RoutePoints> {
        val routePointList: MutableList<RoutePoints> = mutableListOf()
        val sdb = readableDatabase
        val jquery = "SELECT * FROM $TABLE_ROUTE_POINT" +
                " INNER JOIN $TABLE_ROUTE_NAME ON $TABLE_ROUTE_POINT.$TRACK_ID = $TABLE_ROUTE_NAME.$UID" +
                " WHERE $TABLE_ROUTE_NAME.$UID = ?;"

        val cursor: Cursor = sdb.rawQuery(jquery, arrayOf(id.toInt().toString()))

        while (cursor.moveToNext()) {
            val type = cursor.getString(cursor.getColumnIndex(POINT_TYPE))
            val p: RoutePoints = object : RoutePoints {
                override val lat = cursor.getDouble(cursor.getColumnIndex("lat"))
                override val lon: Double = cursor.getDouble(cursor.getColumnIndex("lon"))
                override val type = MapBoxStore.PointType.from(type)
            }
            routePointList.add(p)
        }
        cursor.close()
        return routePointList
    }

    fun deleteTrackById(id: Int) {
        val sdb = readableDatabase
        val jqueryDeleteFromRouteName = "DELETE FROM $TABLE_ROUTE_NAME" +
                " WHERE $UID = ?;"

        sdb.execSQL(jqueryDeleteFromRouteName, arrayOf(id.toString()))
       //  c.close()
        deleteRoutePoints(id)
    }

    fun renameTrack(id: Int, routeName: String) {
        val sdb = readableDatabase
        val query = "UPDATE $TABLE_ROUTE_NAME SET $NAME=? WHERE $UID=?;"
        sdb.execSQL(query, arrayOf(routeName, id.toString()))
    }

    private fun deleteRoutePoints(id: Int) {
        val sdb = readableDatabase
        val jqueryDeleteFromRoutePoints = "DELETE FROM $TABLE_ROUTE_POINT" +
                " WHERE $TRACK_ID = ?;"
        sdb.execSQL(jqueryDeleteFromRoutePoints, arrayOf(id.toString()))
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

    private fun isValueExist(value: String, sdb: SQLiteDatabase): Int {
        val jquery = "SELECT * FROM $TABLE_SETTING_PARAM WHERE $NAME=?"
        val cursor: Cursor = sdb.rawQuery(jquery, arrayOf(value))
        var id = -1
        if (0 < cursor.count) {
            cursor.moveToFirst()
            id = cursor.getInt(cursor.getColumnIndex(UID))
        }
        cursor.close()
        return id
    }


    fun onUnsubscribe() {
        _onDestroy.onComplete()
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

        val jquery = "SELECT * FROM $TABLE_SETTING_MAP;"
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


        val jquery = "SELECT * FROM $TABLE_SETTING_MAP;"
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