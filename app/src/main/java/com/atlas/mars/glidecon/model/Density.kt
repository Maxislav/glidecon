package com.atlas.mars.glidecon.model

import android.content.Context

class Density(context: Context) {
    var density: Float

    var widthPixels: Int
    var heightPixels: Int
    init {
        val displayMetrics = context.resources.displayMetrics
        density = displayMetrics.density
        widthPixels = displayMetrics.widthPixels
        heightPixels = displayMetrics.heightPixels
    }
}