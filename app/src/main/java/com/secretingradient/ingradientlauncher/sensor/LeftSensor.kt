package com.secretingradient.ingradientlauncher.sensor

import android.content.Context
import android.util.AttributeSet
import com.secretingradient.ingradientlauncher.R

class LeftSensor : BaseSensor {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setImageResource(R.drawable.ic_keyboard_arrow_left_white_24dp)
    }

}