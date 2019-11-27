package com.secretingradient.ingradientlauncher.drag

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

class DragLayer: FrameLayout {
    var draggableView: View? = null
        set(value) {
            realState.loadState(field)
            removeView(field)
            realState.parent?.addView(field)

            realState.saveState(value)
            realState.parent?.removeView(value)
            value?.let { addView(it) }

            field = value
        }
    var realState = RealState()
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        draggableView?.let {
            it.translationX = event.x - it.width / 2
            it.translationY = event.y - it.height/ 2
        }
        return false
    }

    class RealState {
        var parent: ViewGroup? = null
        var translationX = 0f
        var translationY = 0f

        fun saveState(v: View?) {
            parent = v?.parent as? ViewGroup
            translationX = v?.translationX ?: 0f
            translationY = v?.translationY ?: 0f
        }

        fun loadState(v: View?) {
            if (v == null)
                return
            v.translationX = translationX
            v.translationY = translationY
        }
    }
}
