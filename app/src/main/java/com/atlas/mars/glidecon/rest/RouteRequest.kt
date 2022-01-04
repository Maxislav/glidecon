package com.atlas.mars.glidecon.rest

import com.atlas.mars.glidecon.BuildConfig
import com.atlas.mars.glidecon.store.MapBoxStore
import com.google.gson.annotations.SerializedName
import com.mapbox.mapboxsdk.geometry.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class RouteRequest {
    private val BASE_URL: String = BuildConfig.URL_API_MAPQUEST
    private val KEY: String = BuildConfig.MAP_QUEST_KEY
    var retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    var myApiEndpointInterface = retrofit.create(MyApiEndpointInterface::class.java)

    interface MyApiEndpointInterface {
        @GET("/directions/v2/route")
        fun getCurrentWeatherByLatLng(
                @Query("key") key: String?,
                @Query("from") from: String?,
                @Query("to") to: String?,
                @Query("routeType") routeType: String?,
                @Query("unit") unit: String?,
                @Query("fullShape") fullShape: Boolean?,
        ): Call<Result>?
    }

    enum class RouteTypeApi(var type: String){
        FASTEST("fastest"),
        SHORTEST("shortest");

    }

    fun restCar(from: LatLng, to: LatLng, routeType:  MapBoxStore.RouteType, cb: (shapePoints: List<Double>?, t: Throwable?) -> Unit) {

        // val _routeType = if (routeType == MapBoxStore.RouteType.CAR)  RouteTypeApi.FASTEST.type else RouteTypeApi.SHORTEST.type;

        myApiEndpointInterface.getCurrentWeatherByLatLng(
                KEY,
                "${from.latitude},${from.longitude}",
                "${to.latitude},${to.longitude}",
                if (routeType == MapBoxStore.RouteType.CAR)  RouteTypeApi.FASTEST.type else RouteTypeApi.SHORTEST.type,
                "k",
                true

        )?.enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>?, response: Response<Result>?) {
                val r: Result? = response?.body()
                val list = r?.route?.shape?.shapePoints
                list
                cb(list, null)
            }

            override fun onFailure(call: Call<Result>?, t: Throwable?) {
                // TODO("Not yet implemented")
                cb(null, t)
            }

        })
    }


    class Result {
        @SerializedName("route")
        var route: Route? = null

    }

    class Route {
        var shape: Shape? = null
    }


    class Shape {
        var shapePoints: List<Double>? = null
    }
}