package com.secretingradient.ingradientlauncher.drag

import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
    val realState = RealState()
    private val newTransformMatrix = Matrix()

    fun onTouchEvent(event: MotionEvent): Boolean {
        dragContext = currentDragContext
        dragEvent.onTouchEvent(event, dragContext)
        val dragContext = dragContext
            ?: return false

        if (event.action == MotionEvent.ACTION_DOWN || isStartDragRequested) {
            isStartDragRequested = false
            val draggable = dragContext.getDraggableUnder(dragEvent.touchPointRaw, newTransformMatrix)
                ?: return false
            dragEvent.setDraggableView(draggable, newTransformMatrix)
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
        dragContext?.onDragEnded(dragEvent)
        dragLayer.draggableView = null
        dragEvent.onStopDrag()
    }

    fun startDragRequest() {
        isStartDragRequested = true
        currentDragContext?.isDragEnabled = true
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