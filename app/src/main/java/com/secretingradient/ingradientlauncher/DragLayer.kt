package com.secretingradient.ingradientlauncher

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

class DragLayer : FrameLayout {
    val gestureHelper = GestureHelper(context)
    var draggedView: View? = null
    val locationOnScreen = IntArray(2)
    val reusablePoint = IntArray(2)

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        draggedView?.let {
            it.translationX = event.rawX - locationOnScreen[0] - it.left - it.width/2
            it.translationY = event.rawY - locationOnScreen[1] - it.top - it.height/2
//            println("${it.translationY}")
        }
        return false
    }

    fun addToDrag(v: View) {
        v.getLocationOnScreen(reusablePoint)
        (v.parent as? ViewGroup)?.removeView(v)
        addView(v)
        draggedView = v
        v.translationX = reusablePoint[0].toFloat() - locationOnScreen[0] + v.width/2
        v.translationY = reusablePoint[1].toFloat() - locationOnScreen[1] + v.height/2
    }

    fun removeDrag(v: View?) {
        v?.translationX = 0f
        v?.translationY = 0f
        removeView(v)
        draggedView = null
    }

    override fun onAttachedToWindow() {
        println("${locationOnScreen[0]} ${locationOnScreen[1]}")
        getLocationOnScreen(locationOnScreen)
        super.onAttachedToWindow()
    }
}