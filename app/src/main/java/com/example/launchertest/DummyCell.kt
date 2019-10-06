package com.example.launchertest

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout


class DummyCell : FrameLayout {
    lateinit var relativePosition: Point // the position within one ScreenGrid (not considering page number)
    var position: Int = -1
    private val bgcolor = Color.argb(40,0,0,0)
    var shortcut: AppShortcut?
        get() = getChildAt(0) as? AppShortcut
        set(value) {if (value != null) addView(value) else removeAllViews()}

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(bgcolor)
    }

    constructor(context: Context, position: Int, relativePosition: Point) : super(context) {
        this.position = position
        this.relativePosition = relativePosition
        layoutParams = GridLayout.LayoutParams(GridLayout.spec(relativePosition.y),GridLayout.spec(relativePosition.x))
        layoutParams.width = 0
        layoutParams.height = 0
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    fun defaultState() {
        shortcut?.translationX = 0f
        shortcut?.translationY = 0f
        setBackgroundColor(bgcolor)
    }

    override fun onViewAdded(child: View?) {
        if (childCount > 1) {
            throw LauncherException("$this can only have 1 child")
        }
        child?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        super.onViewAdded(child)
    }

    fun isEmptyCell(): Boolean {
        if (childCount == 0)
            return true
        return false
    }

    override fun toString(): String {
        return "${javaClass.simpleName}: ${hashCode().toString(16)} $position ${shortcut?.appInfo?.label}"
    }
}