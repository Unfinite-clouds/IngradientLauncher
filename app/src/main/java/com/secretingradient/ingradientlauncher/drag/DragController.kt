package com.secretingradient.ingradientlauncher.drag

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.secretingradient.ingradientlauncher.LauncherActivity

class DragController(val dragLayer: DragLayer) {
    val launcher = (dragLayer.context as LauncherActivity).launcher
    var dragContext: DragContext? = null
        set(value) {
            if (field != value) {
                if (draggable != null)
                    setHovered(null, draggable!!)
                if (value == null)
                    draggable = null
                field?.onEndDrag()
            }
            field = value
        }
    val currentDragContext: DragContext?
        get() = launcher.currentStage.dragContext
    val isDrag
        get() = draggable != null && currentDragContext != null
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
            val oldHovered = hovered
            _hovered_field = newHovered
            oldHovered?.onHoverOut(draggable as View)
            newHovered?.onHoverIn(draggable as View)
        }
    }
    private val _reusablePoint = IntArray(2)
    private var requestStartDrag = false
    var realState = RealState()

    fun onTouchEvent(event: MotionEvent): Boolean {
        dragContext = currentDragContext

        if (event.action == MotionEvent.ACTION_DOWN || requestStartDrag) {
            requestStartDrag = false
            draggable = dragContext?.getDraggableUnder(getPointLocal(event, dragContext!!))
        }

        if (!isDrag) return false

        dragContext!!.onDrag(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_MOVE -> {
                draggable?.let {
                    val pointLocal = getPointLocal(event, dragContext!!)
                    val newHovered = dragContext?.getHoverableUnder(pointLocal)
                    setHovered(newHovered, it)
                    it.onDragMoved()
                    hovered?.onHoverMoved(it as View, pointLocal)
                }
            }
            else -> {
                stopDrag()
            }
        }
        dragLayer.onTouchEvent(event)
        return true
    }

    fun forceStartDrag() {
        requestStartDrag = true
        currentDragContext?.isDragEnabled = true
    }

    fun stopDrag() {
        dragContext?.onEndDrag()
        if (draggable != null) {
            hovered?.onHoverEnd(draggable!! as View)
            _hovered_field = null
        }
        draggable = null
    }

    private fun getPointLocal(event: MotionEvent, dragContext: DragContext): IntArray {
        _reusablePoint[0] = event.rawX.toInt()
        _reusablePoint[1] = event.rawY.toInt()
        dragContext.toPointLocal(_reusablePoint)
        return _reusablePoint
    }

    class RealState {
        var parent: ViewGroup? = null
        var translationX = 0f
        var translationY = 0f

        fun saveState(v: View?) {
            parent = v?.parent as? ViewGroup
            translationX = v?.translationX ?: 0f
            translationY = v?.translationY ?: 0f
        }

        fun loadState(v: View?) {
            if (v == null)
                return
            v.translationX = translationX
            v.translationY = translationY
        }
    }
}