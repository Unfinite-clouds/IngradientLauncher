package com.secretingradient.ingradientlauncher

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.children
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.truncate

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
    private var snapLayout: SnapLayout? = null
    private val snapX
        get() = snapLayout!!.snapStepX
    private val snapY
        get() = snapLayout!!.snapStepY
    private var resizableWidget: AppWidgetHostView? = null
    val snapLoc = IntArray(2)

    fun attachToWidget(widget: AppWidgetHostView) {
        snapLayout = widget.parent as? SnapLayout ?: throw LauncherException("widget must be added to snapLayout")
        val lp = (widget.layoutParams as SnapLayout.SnapLayoutParams)
        val widgetLoc = snapLayout!!.snapPositionToPoint(lp.position)
        resizableWidget = widget
        layoutParams = FrameLayout.LayoutParams(widget.layoutParams.width, widget.layoutParams.height)
        snapLayout!!.getLocationOnScreen(snapLoc)
        translationX = widgetLoc.x + snapLoc[0].toFloat()
        translationY = widgetLoc.y + snapLoc[1].toFloat()
//        requestLayout()
    }

    fun stopResize() {
        snapLayout = null
        resizableWidget = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val widget = resizableWidget ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                side = 0
                r = translationX + right.toFloat()
                b = translationY + bottom.toFloat()
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
                        if ((r - translationX - event.x).toInt() >= minWidth) {
                            translationX += truncate(event.x)
                            layoutParams.width -= event.x.toInt()
                        }
                    }
                    INDEX_TOP -> {
                        if ((b - translationY - event.y).toInt() >= minHeight) {
                            translationY += truncate(event.y)
                            layoutParams.height -= event.y.toInt()
                        }
                    }
                    INDEX_RIGHT -> layoutParams.width = max(minWidth, event.x.toInt())
                    INDEX_BOTTOM -> layoutParams.height = max(minHeight, event.y.toInt())
                }
                resizeWidgetIfNeeded(widget, side)
                requestLayout()
            }
            MotionEvent.ACTION_UP -> {
                val lp = widget.layoutParams as SnapLayout.SnapLayoutParams
                val cellLoc = snapLayout!!.snapPositionToPoint(lp.position)
                layoutParams.width = lp.snapWidth * snapX
                layoutParams.height = lp.snapHeight * snapY
                translationX = cellLoc.x + snapLoc[0].toFloat()
                translationY = cellLoc.y + snapLoc[1].toFloat()
                requestLayout()
            }

        }
        return true
    }

    fun resizeWidgetIfNeeded(widget: AppWidgetHostView, side: Int) {
        val lp = widget.layoutParams as SnapLayout.SnapLayoutParams
        val cellLoc = snapLayout!!.snapPositionToPoint(lp.position)
        val deltaL = (translationX - snapLoc[0] - cellLoc.x).toInt()
        val deltaR = layoutParams.width - lp.snapWidth * snapX
        val deltaT = (translationY - snapLoc[1] - cellLoc.y).toInt()
        val deltaB = layoutParams.height - lp.snapHeight * snapY
        var changed = true
        when {
            side == INDEX_LEFT && abs(deltaL) > snapX -> {
                lp.position += deltaL / snapX
                lp.snapWidth -= deltaL / snapX
            }
            side == INDEX_RIGHT && abs(deltaR) > snapX -> {
                lp.snapWidth += deltaR / snapX
            }
            side == INDEX_TOP && abs(deltaT) > snapY -> {
                println("$deltaT, $snapY")
                lp.position += deltaT / snapY * snapLayout!!.snapCountX
                lp.snapHeight -= deltaT / snapY
            }
            side == INDEX_BOTTOM && abs(deltaB) > snapY -> {
                println("$deltaB, $snapY")
                lp.snapHeight += deltaB / snapY
            }
            else -> changed = false
        }

        if (changed) {
            // currently only for portrait screen
            lp.computeSnapBounds(snapLayout!!)
            widget.updateAppWidgetSize(null, lp.snapWidth * snapX, lp.snapHeight * snapY, lp.snapWidth * snapX, lp.snapHeight * snapY)
            widget.requestLayout()
        }
    }
}