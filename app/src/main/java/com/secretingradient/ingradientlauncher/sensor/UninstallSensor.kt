package com.secretingradient.ingradientlauncher.sensor

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.element.AppView

class UninstallSensor : BaseSensor {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setImageResource(R.drawable.ic_delete_white_24dp)
    }

    override fun onHoverIn(draggedView: View) {
        if (draggedView is AppView)
            super.onHoverIn(draggedView)
    }

    override fun onHoverEnd(v: View) {
        if (v is AppView) {
            v.intentToUninstall()
            super.onHoverEnd(v)
        }
    }

}