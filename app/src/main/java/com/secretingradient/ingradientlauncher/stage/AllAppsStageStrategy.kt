package com.secretingradient.ingradientlauncher.stage

import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.sensor.BaseSensor

private abstract class AllAppsStageStrategy(layout: LauncherRootLayout) : BaseStage(layout), View.OnTouchListener, View.OnLongClickListener {
    lateinit var currentSnapLayout: SnapLayout
    var shouldIntercept = false
    val sensorInfo = BaseSensor(context)
    val sensorUninstall = BaseSensor(context)
    val sensorUp = BaseSensor(context)
    var selectedView: View? = null
    var lastHoveredView: View? = null
    val touchPoint = Point()
    var isDrag = false

    fun preDispatch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            touchPoint.set(event.x.toInt(), event.y.toInt())
            selectedView = trySelect(findViewUnder(touchPoint))
            lastHoveredView = selectedView
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!isDrag)
            return false

        touchPoint.set(event.x.toInt(), event.y.toInt())

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (selectedView != null) {
                    val hoveredView = findViewUnder(touchPoint, lastHoveredView)
                    if (hoveredView != lastHoveredView && hoveredView != null) {
                        if (lastHoveredView != null)
                            onExitHover(lastHoveredView!!)
                        onHover(selectedView!!, hoveredView)
                    }
                    lastHoveredView = hoveredView
                }
            }

            MotionEvent.ACTION_UP -> {
                if (selectedView != null) {
                    val hoveredView = findViewUnder(touchPoint, lastHoveredView)
                    if (hoveredView != null) {
                        performAction(selectedView!!, hoveredView)
                    }
                    lastHoveredView = hoveredView
                }
                endDrag()
            }
        }
        return true
    }

    override fun onLongClick(v: View): Boolean {
        startDrag()
        return true
    }

    private fun startDrag() {
        isDrag = true
        // show 3 sensors
        // disallow v+h-scrolls
        shouldIntercept = true
    }

    private fun endDrag() {
        isDrag = false
        // hide 3 sensors
        shouldIntercept = false
    }

    fun trySelect(v: View?): View? {
        if (v is AppView) {
            // play animations
        }
        return v as? AppView
    }


    fun onHover(selectedView: View, hoveredView: View) {
        when (hoveredView) {
            is BaseSensor -> hoveredView.onSensored(selectedView)
        }
    }
    fun onExitHover(hoveredView: View) {
        when (hoveredView) {
            is BaseSensor -> hoveredView.onExitSensor()
        }
    }
    fun performAction(selectedView: View, hoveredView: View) {
        when (hoveredView) {
            is BaseSensor -> hoveredView.onPerformAction(selectedView)
        }
    }

}