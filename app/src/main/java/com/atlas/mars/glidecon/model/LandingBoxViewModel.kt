package com.atlas.mars.glidecon.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atlas.mars.glidecon.util.LocationUtil
import com.mapbox.mapboxsdk.geometry.LatLng
import java.text.DecimalFormat


class LandingBoxViewModel : ViewModel() {


    var ratioFly = MutableLiveData<String>()
    var ratioFlyFinal = MutableLiveData<String>()
    var angle = MutableLiveData<Double>()
    val startLatLng = MutableLiveData<LatLng>()
    var angleToView = MutableLiveData<String>()

    val startLatLngToView = MutableLiveData<String>()

    var definePointClick = false

    init {
        ratioFly.value = 0.0.toString()
        ratioFlyFinal.value = 0.0.toString()
        angle.value = 0.0
        angleToView.value = DecimalFormat("#").format(normalizeAngle(angle.value!!))
        startLatLngToView.value = "--"
    }

    fun updateRatioFly(v: String) {
        ratioFly.postValue(v)
    }

    fun setAngle(v: Double?) {
        angle.postValue(v)
        angleToView.postValue(DecimalFormat("#").format(normalizeAngle(v!!)))
    }

    fun setRatio(fly: String, final: String) {
        ratioFly.value = fly
        ratioFlyFinal.value = final
    }

    fun setStartLatLng(ll: LatLng?) {
        startLatLng.value = ll
        if (ll != null) {
            startLatLngToView.value = "${formatLatLng(ll.latitude)} ${formatLatLng(ll.longitude)}"
        }else {
            startLatLngToView.value = "--"
        }
    }

    private fun formatLatLng(l: Double): String {
        return DecimalFormat("#.###").format(l)
    }

    override fun onCleared() {
        super.onCleared()
    }

    private fun normalizeAngle(angle: Double): Double {
        return LocationUtil().bearingNormalize(angle)
    }
}