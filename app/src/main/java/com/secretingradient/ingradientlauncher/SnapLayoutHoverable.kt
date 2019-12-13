package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.secretingradient.ingradientlauncher.data.Data
import com.secretingradient.ingradientlauncher.data.Dataset
import com.secretingradient.ingradientlauncher.data.Info
import com.secretingradient.ingradientlauncher.drag.DragTouchEvent
import com.secretingradient.ingradientlauncher.drag.Hoverable
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.FolderView
import com.secretingradient.ingradientlauncher.element.WidgetView
import com.secretingradient.ingradientlauncher.stage.PagedStage2

class SnapLayoutHoverable : SnapLayout, Hoverable {
    val ghostView = GhostView(context)
    val launcher
        get() = (context as LauncherActivity).launcher
    val dataset: Dataset<Data, Info>
        get() = (launcher.currentStage as PagedStage2).dataset
    val debugPointF = PointF() // todo remove
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
        println("onHoverIn ${this.className()} ${event.transformMatrixH}")
        onHoverMoved(event)
        ghostView.visibility = View.VISIBLE
    }

    override fun onHoverOut(event: DragTouchEvent) {
        println("onHoverOut ${this.className()} ${event.transformMatrixH}")
        ghostView.visibility = View.GONE
    }

    companion object {
        val _reusablePoint = Point()
        val _reusablePointF = PointF()
    }

    override fun onHoverMoved(event: DragTouchEvent) {
//        println("onHoverMoved ${this.className()} ${event.transformMatrix}")
        event.getTransformedPointH(debugPointF)
        _reusablePoint.set(debugPointF.x.toInt(), debugPointF.y.toInt())
        invalidate()

        val pos = snapToGrid(_reusablePoint, 2)
        moveView(ghostView, pos)
    }

    override fun onHoverEnded(event: DragTouchEvent) {
        println("onHoverEnded ${this.className()} ${event.transformMatrixH}")
        ghostView.visibility = View.GONE

        event.getTransformedPointH(_reusablePointF)
        _reusablePoint.set(debugPointF.x.toInt(), debugPointF.y.toInt())

        val pos = snapToGrid(_reusablePoint, 2)
        val draggable = event.draggableView as View
        val lp = draggable.layoutParams as? SnapLayoutParams
        if (lp == null)
            draggable.layoutParams = SnapLayoutParams(pos, 2, 2)  // todo widgets?
        else
            lp.position = pos

        (draggable.parent as? ViewGroup)?.removeView(draggable)
        addView(draggable)

        val pagedPos = getPagedPosition(pos)
        val info = when(draggable) {
            is AppView -> draggable.info!!
            is FolderView -> draggable.info
            is WidgetView -> draggable.info
            else -> throw LauncherException("draggable isn't element")
        }
        addAction2 { dataset.put(pagedPos, info, false, false) }
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

    fun getPagedPosition(pos: Int): Int {
        val stage = launcher.currentStage as PagedStage2
        return stage.getPagedPosition(pos, this)
    }

    fun addAction2(f: () -> Unit) {
        launcher.currentStage.dragContext?.pendingActions?.add(1, f)
    }
}