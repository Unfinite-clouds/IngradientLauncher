package com.secretingradient.ingradientlauncher.drag

import android.graphics.Matrix
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import com.secretingradient.ingradientlauncher.LauncherActivity
import com.secretingradient.ingradientlauncher.Point
import com.secretingradient.ingradientlauncher.PointF

abstract class DragContext {
    var isDragEnabled: Boolean = false
    abstract val contentView: ViewGroup
    val dragController
        get() = (contentView.context as LauncherActivity).dragController
    val pendingActions = mutableListOf<()->Unit>()

    abstract fun onDrag(event: DragTouchEvent)

    open fun onDragEnded(event: DragTouchEvent) {
        if (pendingActions.size == 2) {
            pendingActions[0]()
            pendingActions[1]()
//        pendingActions[0] = {}
//        pendingActions[1] = {}
//            dataset.dumpData()
        } else {
            println("pendingActions canceled")
        }
        pendingActions.clear()
    }

    open fun returnThisDraggable(v: Draggable) = false

    open fun returnThisHoverable(v: Hoverable) = false

    fun getDraggableUnder(touchPointRaw: PointF, matrixOut: Matrix): Draggable? {
        if (!isDragEnabled) return null
        val d = hitTraversal<Draggable>(touchPointRaw, matrixOut) {
            if (it is Draggable)
                returnThisDraggable(it as Draggable)
            else
                false
        } as? Draggable
        return d
    }

    fun getHoverableUnder(touchPointRaw: PointF, matrixOut: Matrix): Hoverable? {
        val h = hitTraversal<Hoverable>(touchPointRaw, matrixOut) {
            if (it is Hoverable)
                returnThisHoverable(it as Hoverable)
            else
                false
        } as? Hoverable
        return h
    }

    private inline fun <reified T> hitTraversal(touchPointRaw: PointF, transformMatrixOut: Matrix, returnThis: (v: View) -> Boolean = {false}) : View? {
        // Attention! all scales of contentView's parents will be not applied to the matrix
        transformMatrixOut.reset()  // it will be a matrix of view that was hitted
        tmpMatrixTraversal.reset()
        var nextParent: ViewGroup? =  contentView.parent as? ViewGroup
        nextParent?.getLocationOnScreen(reusablePoint.asArray())  //  takes scales into account
        tmpMatrixTraversal.postTranslate(reusablePoint.x.toFloat(), reusablePoint.y.toFloat())

        var hittedView: View?
        var view: View? = null

        traversal@ while (nextParent is ViewGroup) {
            hittedView = null
            for (i in nextParent.childCount - 1 downTo 0) {
                val child = nextParent.getChildAt(i)
                tmpMatrix.set(tmpMatrixTraversal)
                transformMatrixToChild(child, tmpMatrix)
                if (child.visibility == View.VISIBLE && hitView(child, touchPointRaw, tmpMatrix)) {
                    hittedView = child
                    if (returnThis(child)) {
                        view = child
                        transformMatrixOut.set(tmpMatrix)
                        break@traversal  // returns exactly this view
                    }
                    if (child is T) {
                        view = child // we are looking for the topmost inner view of type T under the touch point
                        transformMatrixOut.set(tmpMatrix)
                    }
                    break
                }
            }
            tmpMatrixTraversal.set(tmpMatrix)
            nextParent = hittedView as? ViewGroup
        }

//        println("hitted = ${view.className()}")
        return view
    }

    private fun transformMatrixToChild(child: View, matrix: Matrix) {
        matrix.preTranslate(child.x, child.y)
        matrix.preScale(child.scaleX, child.scaleY, child.pivotX, child.pivotY)
    }

    private fun hitView(v: View, touchPointRaw: PointF, parentMatrix: Matrix): Boolean {
        reusableRectF.set(0f, 0f, v.width.toFloat(), v.height.toFloat())
        parentMatrix.mapRect(reusableRectF)
        return reusableRectF.contains(touchPointRaw.x, touchPointRaw.y)
    }

    companion object {
        private val reusablePoint = Point()
        private val reusableRectF = RectF()
        private val reusableMatrix = Matrix()
        private val tmpMatrix = Matrix()
        private val tmpMatrixTraversal = Matrix()
    }
}
