package com.secretingradient.ingradientlauncher.drag

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import com.secretingradient.ingradientlauncher.LauncherException
import com.secretingradient.ingradientlauncher.className

abstract class DragContext {
    abstract var canStartDrag: Boolean
    abstract val draggableHandlers: MutableList<DraggableHandler<*>>
    abstract val hoverableHandlers: MutableList<HoverableHandler<*>>  // order does matter!
    abstract val contentView: ViewGroup
    fun toPointLocal(pointGlobal: IntArray) {
        contentView.getLocationOnScreen(_reusablePoint)
        pointGlobal[0] -= _reusablePoint[0]
        pointGlobal[1] -= _reusablePoint[1]
    }
    fun getDraggableHandlerUnder(pointLocal: IntArray): DraggableHandler<*>? {
        if (!canStartDrag)
            return null
        draggableHandlers.forEach {
            if (hitView(it.v, pointLocal))
                return it
        }
        return null
    }
    fun getHoveredViewUnder(pointLocal: IntArray, draggedView: View): HoverableHandler<*>? {
        hoverableHandlers.forEachIndexed {i, it ->
            if (it.v != draggedView && hitView(it.v, pointLocal))
                return it
        }
        return null
    }
    private fun hitView(v: View, pointLocal: IntArray): Boolean {
        getLocationIn(contentView, v, _reusablePoint)
        _reusableRect.set(_reusablePoint[0], _reusablePoint[1], _reusablePoint[0] + v.width, _reusablePoint[1] + v.height)
        return _reusableRect.contains(pointLocal[0], pointLocal[1])
    }
    private fun getLocationIn(viewGroup: ViewGroup, v: View, pointOut: IntArray) {
        if (!v.isAttachedToWindow) throw LauncherException("${v.className()} isn't attached")
        pointOut[0] = 0
        pointOut[1] = 0
        var parent: View = v
        while (parent != viewGroup) {
            pointOut[0] += parent.x.toInt()
            pointOut[1] += parent.y.toInt()
            parent = parent.parent as? ViewGroup ?: throw LauncherException("view ${v.className()} must be a subchild of viewGroup ${viewGroup.className()}")
        }
        println(pointOut.joinToString(", "))
    }
    companion object {
        private val _reusableRect = Rect()
        private val _reusablePoint = IntArray(2)

    }
}
