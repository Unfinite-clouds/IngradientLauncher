package com.secretingradient.ingradientlauncher.sensor

import android.content.Context
import android.view.View
import android.widget.ImageView

/*abstract*/ class BaseSensor(context: Context) : ImageView(context) {
    fun onSensored(v: View) {}
    fun onExitSensor() {}
    fun onPerformAction(v: View) {}
}