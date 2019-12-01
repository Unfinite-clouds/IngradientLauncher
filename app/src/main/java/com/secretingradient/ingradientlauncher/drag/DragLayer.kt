package com.secretingradient.ingradientlauncher.drag

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.secretingradient.ingradientlauncher.LauncherActivity

class DragLayer: FrameLayout {
    var draggableView: View? = null
        set(value) {
            realState.loadState(field)
            if (field?.parent == this) {
                // revert to last state
                this.removeView(field)
                realState.parent?.addView(field)
            }

            realState.saveState(value)
            realState.parent?.removeView(value)
            value?.let { addView(it) }

            field = value
        }
    val dragController
        get() = (context as LauncherActivity).dragController
    val realState
        get() = dragController.realState

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        draggableView?.let {
            it.translationX = event.x - it.width / 2
            it.translationY = event.y - it.height/ 2
        }
        return false
    }


}
