package com.secretingradient.ingradientlauncher

import android.view.MotionEvent

interface OnDispatchTouchEventListener {
    fun onDispatchTouchEvent(event: MotionEvent)
}