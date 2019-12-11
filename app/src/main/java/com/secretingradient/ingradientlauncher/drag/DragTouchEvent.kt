package com.secretingradient.ingradientlauncher.drag

import android.graphics.Matrix
import android.view.MotionEvent
import com.secretingradient.ingradientlauncher.PointF

class DragTouchEvent {
    val touchPointRaw: PointF = PointF()
    val transformedPoint: PointF = PointF()
    /*private*/ val transformMatrix: Matrix = Matrix()  // todo: uncomment "private"
    private val tmpArray = FloatArray(9)
    var draggableView: Draggable? = null
        set(value) {
            if (field != value) {
                field?.onDragEnded(this)
                value?.onDragStarted(this)
                field = value
            }
        }
    var hoverableView: Hoverable? = null
        set(newHovered) {
            if (field != newHovered) {
                val oldHovered = field
                field = newHovered
                oldHovered?.onHoverOut(this)
                newHovered?.onHoverIn(this)
            }
        }
    var dragContext: DragContext? = null
        set(value) {
            if (field != value) {
                hoverableView = null
            }
            field = value
        }

    fun onTouchEvent(event: MotionEvent, dragContext: DragContext?) {
        this.dragContext = dragContext
        touchPointRaw.set(event.rawX, event.rawY)
        transformMatrix.reset()
    }

    fun onStopDrag() {
        draggableView = null
        hoverableView = null
    }

    private fun computeTransformedPoint() {
        transformMatrix.getValues(tmpArray)
        transformedPoint.set((touchPointRaw.x - tmpArray[2])/tmpArray[0], (touchPointRaw.y - tmpArray[5])/tmpArray[4])

        // way 2:
/*        if (!transformMatrix.invert(transformMatrix))
            println("WARNING: matrix wasn't inverted at DragTouchEvent")
        transformMatrix.mapPoints(transformedPoint.asArray(), touchPointRaw.asArray())
        transformMatrix.invert(transformMatrix)*/
    }

    fun transform(newTransform: Matrix) {
        transformMatrix.set(newTransform)
        computeTransformedPoint()
    }
}