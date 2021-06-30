package com.atlas.mars.glidecon.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atlas.mars.glidecon.util.LocationUtil
import java.text.DecimalFormat


class LandingBoxViewModel(a: Double = 0.0) : ViewModel() {


    var ratioFly = MutableLiveData<String>()
    var ratioFlyFinal = MutableLiveData<String>()
    var angle = MutableLiveData<Double>()

    var angleToView = MutableLiveData<String>()


    init {
        ratioFly.value = 0.0.toString()
        ratioFlyFinal.value = 0.0.toString()
        angle.value = a
        // val normalAngle = LocationUtil().bearingNormalize(angle.value!!)
        angleToView.value =  DecimalFormat("#").format(normalizeAngle(angle.value!!))
    }

    fun updateRatioFly(v: String) {
        ratioFly.postValue(v)
    }

    fun setAngle(v: Double?) {
        angle.postValue(v)
        angleToView.postValue(DecimalFormat("#").format(normalizeAngle(v!!)))
    }

    override fun onCleared() {
        super.onCleared()
    }

    private fun normalizeAngle(angle: Double): Double{
       return LocationUtil().bearingNormalize(angle)
    }
}