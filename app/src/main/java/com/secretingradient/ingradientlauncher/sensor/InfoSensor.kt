package com.secretingradient.ingradientlauncher.sensor

import android.content.Context
import android.util.AttributeSet
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.drag.DragTouchEvent
import com.secretingradient.ingradientlauncher.element.AppView

class InfoSensor : BaseSensor {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setImageResource(R.drawable.ic_info_outline_white_24dp)
    }

    override fun onHoverIn(event: DragTouchEvent) {
        if (event.draggableView is AppView)
            super.onHoverIn(event)
    }

    override fun onHoverEnded(event: DragTouchEvent) {
        val v = event.draggableView
        if (v is AppView) {
            v.intentToInfo()
            super.onHoverEnded(event)
        }
    }

}