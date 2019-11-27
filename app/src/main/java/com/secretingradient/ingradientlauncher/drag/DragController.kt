package com.secretingradient.ingradientlauncher.drag

import android.view.MotionEvent

class DragController(val dragLayer: DragLayer) {
    var dragContext: DragContext? = null
        set(value) {
            field = value
            if (draggable != null)
                hovered?.onHoverOut(draggable!!.v)
        }
    var dragEnabled = false
    var draggable: DraggableHandler<*>? = null
        set(value) {
            if (field != value) {
                field?.onDragEnded()
                value?.onDragStarted()
                dragLayer.draggableView = value?.v
                field = value
            }
        }
    private var _hovered_field: HoverableHandler<*>? = null
    val hovered
        get() = _hovered_field
    fun setHovered(newHovered: HoverableHandler<*>?, draggable: DraggableHandler<*>) {
        if (hovered != newHovered) {
            hovered?.onHoverOut(draggable.v)
            newHovered?.onHoverIn(draggable.v)
            _hovered_field = newHovered
        }
    }
    private val _reusablePoint = IntArray(2)

    fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!dragEnabled) return false
        val dragContext = dragContext ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                draggable = dragContext.getDraggableHandlerUnder(getPointLocal(event, dragContext))
            }
            MotionEvent.ACTION_MOVE -> {
                draggable?.let {
                    val newHovered = dragContext.getHoveredViewUnder(getPointLocal(event, dragContext), it.v)
                    setHovered(newHovered, it)
                    it.onDragMoved()
                    hovered?.onHoverMoved(it.v)
                }
            }
            MotionEvent.ACTION_UP -> {
                draggable?.let {
                    hovered?.onHoverOut(draggable!!.v)
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