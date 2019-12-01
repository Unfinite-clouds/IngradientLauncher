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

    override fun onHoverIn(draggedView: View) {
        if (draggedView is AppView) {
            super.onHoverIn(draggedView)
            // animation arrows UP вотак прям вжух-вжУХ-ВЖУХ
        }
    }
}