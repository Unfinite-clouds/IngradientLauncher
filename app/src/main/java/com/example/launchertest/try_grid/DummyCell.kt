package com.example.launchertest.try_grid

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi

class DummyCell : LinearLayout {
    companion object {
        const val STATE_EMPTY = 0
        const val STATE_FILLED = 1
        const val STATE_DRAGGING = 2
    }
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var state: Int = 0

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        state = STATE_FILLED
    }

    fun onOver(x: Float, y: Float){
        val side: String

        // remember that origin of coordinate system is [left, top]
        if (y>x) side = if (y>height-x) "bottom" else "left"
        else side = if (y>height-x) "right" else "top"

        println("$x $y $side")
    }
}