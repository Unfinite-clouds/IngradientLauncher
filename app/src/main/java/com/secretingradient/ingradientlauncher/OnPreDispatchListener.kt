package com.secretingradient.ingradientlauncher

import android.view.MotionEvent

interface OnPreDispatchListener {
    fun onPreDispatch(event: MotionEvent)
}