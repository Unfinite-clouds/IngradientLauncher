package com.secretingradient.ingradientlauncher.drag

import android.graphics.Matrix
import android.view.MotionEvent
import com.secretingradient.ingradientlauncher.PointF
import com.secretingradient.ingradientlauncher.className

class DragTouchEvent {
    val touchPointRaw: PointF = PointF()
    val transformedPoint: PointF = PointF()
    /*private*/ val transformMatrix: Matrix = Matrix()  // todo: uncomment "private"
    private val tmpArray = FloatArray(9)
    var draggableView: Draggable? = null
        private set
    var hoverableView: Hoverable? = null
        private set
    var dragContext: DragContext? = null
        private set(value) {
            if (field != value) {
                setHoverableView(null, null)
            }
            field = value
        }

    fun onTouchEvent(event: MotionEvent, dragContext: DragContext?) {
        this.dragContext = dragContext
        touchPointRaw.set(event.rawX, event.rawY)
    }

    fun onStopDrag() {
        computeTransformedPoint()
        setHoverableView(null, null, true)
        setDraggableView(null, null)
    }

    private fun computeTransformedPoint() {
        transformMatrix.getValues(tmpArray)
        transformedPoint.set((touchPointRaw.x - tmpArray[2])/tmpArray[0], (touchPointRaw.y - tmpArray[5])/tmpArray[4])
    }

    fun setDraggableView(newDraggable: Draggable?, newTransform: Matrix?) {
        if (draggableView != newDraggable) {
            draggableView?.onDragEnded(this)
            if (newDraggable != null) {
                transformMatrix(newTransform!!)
                newDraggable.onDragStarted(this)
            } else {
                resetMatrix()
            }
            draggableView = newDraggable
        }
    }

    fun setHoverableView(newHoverable: Hoverable?, newTransform: Matrix?, isEnd: Boolean = false) {
            if (hoverableView != newHoverable) {
                println("newHoverable = ${newHoverable.className()} - $newTransform")
                val oldHovered = hoverableView
                hoverableView = newHoverable
                oldHovered?.onHoverOut(this)
                if (isEnd)
                    oldHovered?.onHoverEnded(this)
                if (newHoverable != null) {
                    transformMatrix(newTransform!!)
                    newHoverable.onHoverIn(this)
                } else {
                    resetMatrix()
                }
            }
    }

    private fun transformMatrix(newTransform: Matrix) {
        transformMatrix.set(newTransform)
        computeTransformedPoint()
    }

    private fun resetMatrix() {
        transformMatrix.reset()
        computeTransformedPoint()
    }

    fun onMoved() {
        computeTransformedPoint()
        draggableView!!.onDragMoved(this)
        hoverableView?.onHoverMoved(this)
    }

}