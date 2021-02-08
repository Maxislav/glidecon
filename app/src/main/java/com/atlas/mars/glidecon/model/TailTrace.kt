package com.atlas.mars.glidecon.model

import android.content.Context
import android.graphics.Color
import com.atlas.mars.glidecon.R
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.expressions.Expression.rgb
import com.mapbox.mapboxsdk.style.expressions.Expression.rgba

class TailTrace(val style: Style, context: Context) {

    companion object {
        const val SOURCE_ID = "source-tail-trace"
        const val LAYER_ID = "layer-tail-trace"
        const val TAG = "DirectionArea"
    }

    init{
        val source = createSource(SOURCE_ID)
       // val criticalColor = context.resources.getString(R.color.safetyColor)

        style.addLayer(LineLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(3f),
                PropertyFactory.lineOpacity(0.5f),
                PropertyFactory.lineGradient(
                        interpolate(
                                linear(), lineProgress(),
                                stop(0f, rgb(6, 1, 255)), // blue
                                stop(0.1f, rgb(59, 118, 227)), // royal blue
                                stop(0.3f, rgb(7, 238, 251)), // cyan
                                stop(0.5f, rgb(0, 255, 42)), // lime
                                stop(0.7f, rgb(255, 252, 0)), // yellow
                                stop(1f, rgba(255, 30, 0, 0)) // red
                        ))
                //PropertyFactory.lineColor(Color.parseColor(safetyModeColor)),


        ))
    }

    private fun createSource(sourceId: String): GeoJsonSource {
        style.addSource(GeoJsonSource(sourceId))
        return style.getSource(sourceId) as GeoJsonSource
    }
}