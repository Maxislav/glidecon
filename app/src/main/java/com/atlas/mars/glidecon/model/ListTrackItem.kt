package com.atlas.mars.glidecon.model

import java.text.SimpleDateFormat
import java.util.*

class ListTrackItem(public var name: String, private val _distance: Number, private val _date: Date = Date()) {
    val date: String
        get() {
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
            return format.format(_date)
        }

    val distance: String
        get() {
            return "${_distance.toString()}km"
        }

}