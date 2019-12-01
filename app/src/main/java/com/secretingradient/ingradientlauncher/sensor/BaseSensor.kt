package com.secretingradient.ingradientlauncher.sensor

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.secretingradient.ingradientlauncher.drag.Hoverable

abstract class BaseSensor : ImageView, Hoverable {

    var lastAlpha = -1
    var sensorListener: Hoverable? = null
    var disabled = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onHoverIn(draggedView: View) {
        if (!disabled) {
            highlight()
            sensorListener?.onHoverIn(draggedView)
        }
    }
    override fun onHoverOut(draggedView: View) {
        if (!disabled) {
            unhighlight()
            sensorListener?.onHoverOut(draggedView)
        }
    }

    override fun onHoverMoved(draggedView: View, pointLocal: IntArray) {}

    override fun onHoverEnd(draggedView: View) {
        if (!disabled) {
            sensorListener?.onHoverEnd(draggedView)
        }
    }

    fun highlight() {
        lastAlpha = (drawable as BitmapDrawable).paint.alpha
        drawable.alpha = 255
    }

    fun unhighlight() {
        drawable.alpha = lastAlpha
        lastAlpha = -1
    }
}