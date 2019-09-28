package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout

class ScreenRoot : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        println("pre - Dispatch")
        var returned: Boolean = super.dispatchTouchEvent(ev)
        println("post - Dispatch $returned")
        println(" ")
        return returned
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        println("   pre - Intercept")
        var returned: Boolean = super.onInterceptTouchEvent(ev)
        returned = true
        println("   post - Intercept $returned")
        return returned
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        println("   pre - Touch")
        var returned: Boolean = super.onTouchEvent(ev)
        returned = true
        println("   post - Touch $returned")
        return returned
    }


}