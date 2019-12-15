package com.secretingradient.ingradientlauncher.drag

import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View
import com.secretingradient.ingradientlauncher.LauncherActivity

class DragController(val dragLayer: DragLayer) {
    val launcher = (dragLayer.context as LauncherActivity).launcher
    var dragContext: DragContext? = null
        private set(value) {
            if (field != value) {
                field?.onDragEnded(dragEvent)
                if (value == null)
                    stopDrag()
            }
            field = value
        }
    val currentDragContext: DragContext?
        get() = launcher.currentStage.dragContext
    val isDrag
        get() = dragEvent.draggableView != null && currentDragContext != null
    val dragEvent = DragTouchEvent()
    private var isStartDragRequested = false
    private var requestedDraggable: Draggable? = null
    val realState = DragRealState()
    private val newTransformMatrix = Matrix()

    fun onTouchEvent(event: MotionEvent): Boolean {
        dragContext = currentDragContext
        dragEvent.onTouchEvent(event, dragContext)
        val dragContext = dragContext
            ?: return false

        if (event.action == MotionEvent.ACTION_DOWN || isStartDragRequested) {
            val draggable: Draggable? =
                if (isStartDragRequested) {
                    isStartDragRequested = false
                    requestedDraggable ?: dragContext.getDraggableUnder(dragEvent.touchPointRaw, newTransformMatrix)
                } else {
                    dragContext.getDraggableUnder(dragEvent.touchPointRaw, newTransformMatrix)
                }.also { requestedDraggable = null }
                    ?: return false

            dragEvent.setDraggableView(draggable, newTransformMatrix, realState)
            dragLayer.draggableView = draggable as View?
        }

        if (!isDrag) return false

        dragContext.onDrag(dragEvent)

        val hoverable = dragContext.getHoverableUnder(dragEvent.touchPointRaw, newTransformMatrix)
        dragEvent.setHoverableView(hoverable, newTransformMatrix)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_MOVE -> {
                dragEvent.onMoved()
            }
            else -> {
                stopDrag()
            }
        }
        dragLayer.onTouchEvent(event)
        return true
    }

    fun stopDrag() {
        dragEvent.onStopDrag()
        dragContext?.onDragEnded(dragEvent)
        dragLayer.draggableView = null
    }

    fun startDragRequest(draggableView: Draggable? = null) {
        if (!isDrag) {
            isStartDragRequested = true
            requestedDraggable = draggableView
            currentDragContext?.isDragEnabled = true
        }
    }

}