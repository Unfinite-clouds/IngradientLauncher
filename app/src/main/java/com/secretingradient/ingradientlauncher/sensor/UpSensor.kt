package com.secretingradient.ingradientlauncher.sensor

import android.content.Context
import android.util.AttributeSet
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.drag.DragTouchEvent
import com.secretingradient.ingradientlauncher.element.AppView

class UpSensor : BaseSensor {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setImageResource(R.drawable.ic_keyboard_arrow_up_white_24dp)
    }

    override fun onHoverIn(event: DragTouchEvent) {
        if (event.draggableView is AppView) {
            super.onHoverIn(event)
            // animation arrows UP вотак прям вжух-вжУХ-ВЖУХ
        }
    }
}