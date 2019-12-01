package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.secretingradient.ingradientlauncher.drag.Hoverable
import com.secretingradient.ingradientlauncher.element.AppView

class SnapLayoutHoverable : SnapLayout, Hoverable {
    val ghostView = ImageView(context)
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, snapCountX: Int, snapCountY: Int) : super(context, snapCountX, snapCountY)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (ghostView.parent != this) {
            (ghostView.parent as? ViewGroup)?.removeView(ghostView)
            addNewView(ghostView, 0, 2, 2)
            ghostView.setBackgroundColor(Color.LTGRAY)
            ghostView.visibility = View.GONE
        }
    }

    override fun onHoverIn(draggedView: View) {
        println("onHoverIn")
        ghostView.visibility = View.VISIBLE
        println(ghostView.parent)
    }

    override fun onHoverOut(draggedView: View) {
        println("onHoverOut")
        ghostView.visibility = View.GONE
    }

    companion object {
        val _reusablePoint = Point()
        val _reusablePair = IntArray(2)
    }
    override fun onHoverMoved(draggedView: View, pointLocal: IntArray) {
        println("onHoverMoved")
        val dragContext = (context as LauncherActivity).dragController.currentDragContext!!
        dragContext.getLocationIn(dragContext.contentView, this, _reusablePair)
        _reusablePoint.set(_reusablePair[0] + pointLocal[0], _reusablePair[1] + pointLocal[1])
        if (draggedView is AppView) {
            val pos = snapToGrid(_reusablePoint, 2)
            moveView(ghostView, pos)
        }
    }

    override fun onHoverEnd(draggedView: View) {
        println("onHoverEnd")
        ghostView.visibility = View.GONE
    }

    private fun transformGhostView(pos: Int, snapW: Int = 2, snapH: Int = 2) {
        val lp = ghostView.layoutParams as SnapLayoutParams
        lp.position = pos
        lp.snapWidth = snapW
        lp.snapHeight = snapH
    }
}