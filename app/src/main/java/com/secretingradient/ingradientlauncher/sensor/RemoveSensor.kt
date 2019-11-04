package com.secretingradient.ingradientlauncher.sensor

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.secretingradient.ingradientlauncher.R

class RemoveSensor: BaseSensor {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setImageResource(R.drawable.ic_highlight_off_white_24dp)
    }

    override fun onPerformAction(v: View) {
        (v.parent as ViewGroup).removeView(v)
        super.onPerformAction(v)
    }

}