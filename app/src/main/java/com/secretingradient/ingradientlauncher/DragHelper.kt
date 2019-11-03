package com.secretingradient.ingradientlauncher

import android.graphics.Point
import android.view.MotionEvent
import android.view.View

abstract class DragHelper {
    private var isDrag = false
    private var lastHoveredView: View? = null
    var selectedView: View? = null
    val touchPoint = Point()

    open fun startDrag() {
        isDrag = true
    }

    open fun endDrag() {
        isDrag = false
    }

    fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!isDrag)
            return false

        touchPoint.set(event.x.toInt(), event.y.toInt())

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (selectedView != null) {
                    val hoveredView = findViewAt(touchPoint, lastHoveredView)
                    if (hoveredView != lastHoveredView && hoveredView != null) {
                        if (lastHoveredView != null)
                            onExitHover(lastHoveredView!!)
                        onHover(selectedView!!, hoveredView, touchPoint)
                    }
                    lastHoveredView = hoveredView
                }
            }

            MotionEvent.ACTION_UP -> {
                if (selectedView != null) {
                    val hoveredView = findViewAt(touchPoint, lastHoveredView)
                    if (hoveredView != null) {
                        performAction(selectedView!!, hoveredView, touchPoint)
                    }
                    lastHoveredView = hoveredView
                }
                endDrag()
            }
        }
        return true
    }

    abstract fun findViewAt(touchPoint: Point, lastHoveredView: View?) : View?
    abstract fun onExitHover(hoveredView: View)
    abstract fun onHover(selectedView: View, hoveredView: View, touchPoint: Point)
    abstract fun performAction(selectedView: View, hoveredView: View, touchPoint: Point)
}