package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.secretingradient.ingradientlauncher.drag.DragTouchEvent
import com.secretingradient.ingradientlauncher.drag.Hoverable
import com.secretingradient.ingradientlauncher.element.AppView

class SnapLayoutHoverable : SnapLayout, Hoverable {
    val ghostView = GhostView(context)
    val debugPointF = PointF()
    val paint = Paint().apply {
//        style = Paint.Style.STROKE
//        strokeWidth = 3f
        color = Color.RED
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, snapCountX: Int, snapCountY: Int) : super(context, snapCountX, snapCountY)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (ghostView.parent != this) {
            (ghostView.parent as? ViewGroup)?.removeView(ghostView)
            addNewView(ghostView, 0, 2, 2)
            ghostView.visibility = View.GONE
        }
    }

    override fun onHoverIn(event: DragTouchEvent) {
        println("onHoverIn ${this.className()} ${event.transformMatrix}")
        onHoverMoved(event)
        ghostView.visibility = View.VISIBLE
    }

    override fun onHoverOut(event: DragTouchEvent) {
        println("onHoverOut ${this.className()} ${event.transformMatrix}")
        ghostView.visibility = View.GONE
    }

    companion object {
        val _reusablePoint = Point()
    }

    override fun onHoverMoved(event: DragTouchEvent) {
//        println("onHoverMoved ${this.className()} ${event.transformMatrix}")
        debugPointF.set(event.transformedPoint)
        _reusablePoint.set(debugPointF.x.toInt(), debugPointF.y.toInt())
        invalidate()

        val draggedView = event.draggableView
        if (draggedView is AppView) {
            val pos = snapToGrid(_reusablePoint, 2)
            moveView(ghostView, pos)
        }
    }

    override fun onHoverEnded(event: DragTouchEvent) {
        println("onHoverEnded ${this.className()} ${event.transformMatrix}")
        ghostView.visibility = View.GONE
    }

    private fun transformGhostView(pos: Int, snapW: Int = 2, snapH: Int = 2) {
        val lp = ghostView.layoutParams as SnapLayoutParams
        lp.position = pos
        lp.snapWidth = snapW
        lp.snapHeight = snapH
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawCircle(debugPointF.x, debugPointF.y, 10f, paint)
    }
}