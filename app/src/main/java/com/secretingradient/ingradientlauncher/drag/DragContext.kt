package com.secretingradient.ingradientlauncher.drag

import android.graphics.Rect
import android.view.View

abstract class DragContext {
    abstract var canStartDrag: Boolean
    abstract val draggableHandlers: MutableList<DraggableHandler<*>>
    abstract val hoverableHandlers: MutableList<HoverableHandler<*>>  // order does matter!
    abstract fun toPointLocal(pointGlobal: IntArray)
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
        v.getHitRect(_reusableRect)
        return _reusableRect.contains(pointLocal[0], pointLocal[1])
    }
    companion object {
        private val _reusableRect = Rect()
    }
}
