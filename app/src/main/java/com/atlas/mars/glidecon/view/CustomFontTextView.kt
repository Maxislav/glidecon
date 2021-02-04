package com.atlas.mars.glidecon.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.util.*

class CustomFontTextView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {
    init {
        val customTypeface: Typeface = CustomFontTextView.CustomFont.fromString("digital_7").asTypeface(context)
        typeface = customTypeface
    }

    internal enum class CustomFont(private val fileName: String) {
        DIGITAL_7("fonts/digital_7.ttf"), DIGITAL_7_ITALIC("fonts/digital_7_italic.ttf");

        fun asTypeface(context: Context): Typeface {
            return Typeface.createFromAsset(context.assets, fileName)
        }

        companion object {
            fun fromString(fontName: String): CustomFont {
                return com.atlas.mars.glidecon.view.CustomFontTextView.CustomFont.valueOf(fontName.toUpperCase(Locale.US))
            }
        }
    }
}