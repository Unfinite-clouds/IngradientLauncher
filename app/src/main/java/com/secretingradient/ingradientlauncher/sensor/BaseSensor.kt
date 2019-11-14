package com.secretingradient.ingradientlauncher.sensor

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

abstract class BaseSensor : ImageView {

    var lastAlpha = -1
    var sensorListener: SensorListener? = null
    var disabled = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    open fun onSensor(v: View) {
        if (!disabled) {
            highlight()
            sensorListener?.onSensor(v)
        }
    }
    open fun onExitSensor() {
        if (!disabled) {
            unhighlight()
            sensorListener?.onExitSensor()
        }
    }
    open fun onPerformAction(v: View) {
        if (!disabled) {
            sensorListener?.onPerformAction(v)
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

    interface SensorListener {
        fun onSensor(v: View)
        fun onExitSensor()
        fun onPerformAction(v: View)
    }
}