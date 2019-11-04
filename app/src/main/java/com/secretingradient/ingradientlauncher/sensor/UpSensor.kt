package com.secretingradient.ingradientlauncher.sensor

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.element.AppView

class UpSensor : BaseSensor {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setImageResource(R.drawable.ic_keyboard_arrow_up_white_24dp)
    }

    override fun onSensor(v: View) {
        if (v is AppView) {
            super.onSensor(v)
            // animation arrows UP вотак прям вжух-вжУХ-ВЖУХ
        }
    }
}