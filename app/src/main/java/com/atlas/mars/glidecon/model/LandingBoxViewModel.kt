package com.atlas.mars.glidecon.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.DecimalFormat


class LandingBoxViewModel(a: Double = 0.0) : ViewModel() {


    var ratioFly = MutableLiveData<String>()
    var angle = MutableLiveData<Double>()

    var angleToView = MutableLiveData<String>()


    init {
        ratioFly.value = a.toString()
        angle.value = a
        angleToView.value =  DecimalFormat("#").format(angle.value)
    }

    fun updateRatioFly(v: String) {
        ratioFly.postValue(v)
    }

    fun setAngle(v: Double?) {
        angle.postValue(v)
        angleToView.postValue(DecimalFormat("#").format(v))
    }

    override fun onCleared() {
        super.onCleared()
    }
}