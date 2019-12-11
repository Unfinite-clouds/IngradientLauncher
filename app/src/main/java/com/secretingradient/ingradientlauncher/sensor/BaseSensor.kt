package com.secretingradient.ingradientlauncher.sensor

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.ImageView
import com.secretingradient.ingradientlauncher.drag.DragTouchEvent
import com.secretingradient.ingradientlauncher.drag.Hoverable

abstract class BaseSensor : ImageView, Hoverable {

    var lastAlpha = -1
    var sensorListener: Hoverable? = null
    var disabled = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onHoverIn(event: DragTouchEvent) {
        if (!disabled) {
            highlight()
            sensorListener?.onHoverIn(event)
        }
    }
    override fun onHoverOut(event: DragTouchEvent) {
        if (!disabled) {
            unhighlight()
            sensorListener?.onHoverOut(event)
        }
    }

    override fun onHoverMoved(event: DragTouchEvent) {}

    override fun onHoverEnd(event: DragTouchEvent) {
        if (!disabled) {
            sensorListener?.onHoverEnd(event)
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