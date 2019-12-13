package com.secretingradient.ingradientlauncher.drag

import android.graphics.Matrix
import android.view.MotionEvent
import com.secretingradient.ingradientlauncher.PointF
import com.secretingradient.ingradientlauncher.className

class DragTouchEvent {
    lateinit var motionEvent: MotionEvent
    val touchPointRaw: PointF = PointF()
    /*private*/ val transformMatrixH: Matrix = Matrix()  // onDragStarted - for dragableView, other - for hoverableView todo: uncomment "private"
    /*private*/ val transformMatrixD: Matrix = Matrix()  // onDragStarted - for dragableView, other - for hoverableView todo: uncomment "private"
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
    var realState: DragRealState? = null

    fun onTouchEvent(event: MotionEvent, dragContext: DragContext?) {
        motionEvent = event
        this.dragContext = dragContext
        touchPointRaw.set(event.rawX, event.rawY)
    }

    fun onStopDrag() {
        setHoverableView(null, null, true)
        setDraggableView(null, null, null)
    }

    fun getTransformedPointH(pointOut: PointF) {
        transformMatrixH.getValues(tmpArray)
        pointOut.set((touchPointRaw.x - tmpArray[2])/tmpArray[0], (touchPointRaw.y - tmpArray[5])/tmpArray[4])
    }

    fun getTransformedPointD(pointOut: PointF) {
        transformMatrixD.getValues(tmpArray)
        pointOut.set((touchPointRaw.x - tmpArray[2])/tmpArray[0], (touchPointRaw.y - tmpArray[5])/tmpArray[4])
    }

    fun getMatrixHTranslation(pointOut: PointF) {
        transformMatrixH.getValues(tmpArray)
        pointOut.set(tmpArray[2], tmpArray[5])
    }

    fun getMatrixDTranslation(pointOut: PointF) {
        transformMatrixD.getValues(tmpArray)
        pointOut.set(tmpArray[2], tmpArray[5])
    }

    fun setDraggableView(newDraggable: Draggable?, newTransform: Matrix?, realState: DragRealState?) {
        if (draggableView != newDraggable) {
            this.realState = realState

            draggableView?.onDragEnded(this)

            if (newDraggable != null) {
                if (newTransform != null)
                    transformMatrixD.set(newTransform)
                newDraggable.onDragStarted(this)
            }
            draggableView = newDraggable
        }
    }

    fun setHoverableView(newHoverable: Hoverable?, newTransform: Matrix?, isEnd: Boolean = false) {
            if (hoverableView != newHoverable) {
                println("newHoverable = ${newHoverable.className()} - $newTransform")
                val oldHovered = hoverableView
                hoverableView = newHoverable

                if (isEnd)
                    oldHovered?.onHoverEnded(this)
                else
                    oldHovered?.onHoverOut(this)

                if (newHoverable != null) {
                    if (newTransform != null)
                        transformMatrixH.set(newTransform)
                    newHoverable.onHoverIn(this)
                }
            }
    }

    fun onMoved() {
        draggableView!!.onDragMoved(this)
        hoverableView?.onHoverMoved(this)
    }

}