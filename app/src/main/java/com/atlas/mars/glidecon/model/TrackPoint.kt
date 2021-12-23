package com.atlas.mars.glidecon.model

import com.atlas.mars.glidecon.store.MapBoxStore
import com.mapbox.mapboxsdk.geometry.LatLng

class TrackPoint(val point: LatLng, val type: MapBoxStore.PointType) {
}