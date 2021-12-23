package com.atlas.mars.glidecon.model

import java.text.SimpleDateFormat
import java.util.*

class ListTrackItem(var trackId: Int, var name: String, private val _distance: Number, private val _date: String) {
    val date: String
        get() {
            return _date
        }

    val distance: String
        get() {
            return "${_distance.toString()}km"
        }

}