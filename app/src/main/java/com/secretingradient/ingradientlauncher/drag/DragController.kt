package com.secretingradient.ingradientlauncher.drag

import android.view.MotionEvent
import android.view.View

class DragController(val dragLayer: DragLayer) {
    var dragContext: DragContext? = null
        set(value) {
            field = value
            if (draggable != null)
                hovered?.onHoverOut(draggable as View)
        }
    var dragEnabled = false
    var draggable: Draggable? = null
        set(value) {
            if (field != value) {
                field?.onDragEnded()
                value?.onDragStarted()
                dragLayer.draggableView = value as View?
                field = value
            }
        }
    private var _hovered_field: Hoverable? = null
    val hovered
        get() = _hovered_field
    fun setHovered(newHovered: Hoverable?, draggable: Draggable) {
        if (hovered != newHovered) {
            hovered?.onHoverOut(draggable as View)
            newHovered?.onHoverIn(draggable as View)
            _hovered_field = newHovered
        }
    }
    private val _reusablePoint = IntArray(2)

    fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!dragEnabled) return false
        val dragContext = dragContext ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                draggable = dragContext.getDraggableUnder(getPointLocal(event, dragContext))
            }
            MotionEvent.ACTION_MOVE -> {
                draggable?.let {
                    val newHovered = dragContext.getHoverableUnder(getPointLocal(event, dragContext))
                    setHovered(newHovered, it)
                    it.onDragMoved()
                    hovered?.onHoverMoved(it as View)
                }
            }
            MotionEvent.ACTION_UP -> {
                draggable?.let {
                    setHovered(null, it)
                    draggable = null
                }
            }
        }
        dragLayer.onTouchEvent(event)
        return true
    }

    private fun getPointLocal(event: MotionEvent, dragContext: DragContext): IntArray {
        _reusablePoint[0] = event.rawX.toInt()
        _reusablePoint[1] = event.rawY.toInt()
        dragContext.toPointLocal(_reusablePoint)
        return _reusablePoint
    }
}