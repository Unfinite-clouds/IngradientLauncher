package com.secretingradient.ingradientlauncher

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.children
import kotlin.math.abs
import kotlin.math.max

class WidgetResizeFrame(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val reusableRect = Rect()
    private val INDEX_LEFT = 0
    private val INDEX_TOP = 1
    private val INDEX_RIGHT = 2
    private val INDEX_BOTTOM = 3
    var side = 0
    var r = 0f
    var b = 0f
    var minWidth = 100
    var minHeight = 100
    var hostView: AppWidgetHostView? = null
        set(value) {
            field = value
            if (value != null)
                layoutParams = ViewGroup.LayoutParams(value.layoutParams.width, value.layoutParams.height)
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val widget = hostView ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                side = 0
                r = x + right.toFloat()
                b = y + bottom.toFloat()
                println(bottom)
                for (it in children) {
                    if (it is ImageView) {
                        it.getHitRect(reusableRect)
                        reusableRect.inset(-20, -20)
                        if (reusableRect.contains(event.x.toInt(), event.y.toInt()))
                            break
                        side++
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when (side) {
                    INDEX_LEFT -> {
                        if ((r - x - event.x).toInt() >= minWidth) {
                            x += event.x
                            layoutParams.width = (r - x).toInt()
                        }
                    }
                    INDEX_TOP -> {
                        if ((b - y - event.y).toInt() >= minHeight) {
                            y += event.y
                            layoutParams.height = (b - y).toInt()
                        }
                    }
                    INDEX_RIGHT -> layoutParams.width = max(minWidth, event.x.toInt())
                    INDEX_BOTTOM -> layoutParams.height = max(minHeight, event.y.toInt())
                }
                resizeWidgetIfNeeded(widget)
                requestLayout()
            }
            MotionEvent.ACTION_UP -> {
                layoutParams.width = widget.layoutParams.width
                layoutParams.height = widget.layoutParams.height
                x = widget.x
                y = widget.y
                requestLayout()
            }

        }
        return true
    }

    fun resizeWidgetIfNeeded(widget: AppWidgetHostView) {
        val snapX = 100
        val snapY = 100
        val portWidth = layoutParams.width
        val portHeight = layoutParams.height

        if (abs(portWidth - widget.layoutParams.width) > snapX) {
            widget.x = x
            widget.layoutParams.width = portWidth
            widget.updateAppWidgetSize(null, portWidth, portHeight, portWidth, portHeight)
        }

        if (abs(portHeight - widget.layoutParams.height) > snapY) {
            widget.y = y
            widget.layoutParams.height = portHeight
            widget.updateAppWidgetSize(null, portWidth, portHeight, portWidth, portHeight)
        }
    }
}