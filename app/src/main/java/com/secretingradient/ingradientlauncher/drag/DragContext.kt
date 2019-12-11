package com.secretingradient.ingradientlauncher.drag

import android.graphics.Matrix
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import com.secretingradient.ingradientlauncher.*

abstract class DragContext {
    var isDragEnabled: Boolean = false
    abstract val contentView: ViewGroup
    val dragController
        get() = (contentView.context as LauncherActivity).dragController
    val pendingActions = MutableList<()->Unit>(2) {{}}

    abstract fun onDrag(event: DragTouchEvent)

    open fun onDragEnded(event: DragTouchEvent) {
        if (pendingActions.size > 2)
            throw LauncherException("pendingActions.size > 2")
        pendingActions[0]()
        pendingActions[1]()
        pendingActions[0] = {}
        pendingActions[1] = {}
//            dataset.dumpData()
    }

    open fun returnThisDraggable(v: Draggable) = false

    open fun returnThisHoverable(v: Hoverable) = false

    fun getDraggableUnder(dragEvent: DragTouchEvent): Draggable? {
        if (!isDragEnabled) return null
        val d = hitTraversal<Draggable>(dragEvent.touchPointRaw, reusableMatrix) {
            if (it is Draggable)
                returnThisDraggable(it as Draggable)
            else
                false
        } as? Draggable
        dragEvent.transform(reusableMatrix)
        return d
    }
    fun getHoverableUnder(dragEvent: DragTouchEvent): Hoverable? {
        val h = hitTraversal<Hoverable>(dragEvent.touchPointRaw, reusableMatrix) {
            if (it is Hoverable)
                returnThisHoverable(it as Hoverable)
            else
                false
        } as? Hoverable
        dragEvent.transform(reusableMatrix)
        return h
    }

    private fun hitView(v: View, touchPointRaw: PointF, parentMatrix: Matrix): Boolean {
        reusableRectF.set(0f, 0f, v.width.toFloat(), v.height.toFloat())
        parentMatrix.mapRect(reusableRectF)
        return reusableRectF.contains(touchPointRaw.x, touchPointRaw.y)
    }

    private inline fun <reified T> hitTraversal(touchPointRaw: PointF, transformMatrixOut: Matrix, returnThis: (v: View) -> Boolean = {false}) : View? {
        // Attention! all scales of contentView's parents will be not applied to the matrix
        var nextParent: ViewGroup? =  contentView.parent as? ViewGroup
        transformMatrixOut.reset()  // it will be a parent matrix of view that was hitted
        nextParent?.getLocationOnScreen(reusablePoint.asArray())  //  takes scales into account
        transformMatrixOut.postTranslate(reusablePoint.x.toFloat(), reusablePoint.y.toFloat())

        var hittedView: View?
        var view: View? = null

        traversal@ while (nextParent is ViewGroup) {
            hittedView = null
            for (i in nextParent.childCount - 1 downTo 0) {
                val child = nextParent.getChildAt(i)
                tmpMatrix.set(transformMatrixOut)
                transformMatrixToChild(child, tmpMatrix)
                if (child.visibility == View.VISIBLE && hitView(child, touchPointRaw, tmpMatrix)) {
                    hittedView = child
                    if (returnThis(child)) {
                        view = child
                        transformMatrixToChild(child, transformMatrixOut)
                        break@traversal  // returns exactly this view
                    }
                    if (child is T) {
                        view = child
                        break  // we are looking for the topmost view under the touch point to return
                    }
                }
            }
            nextParent = hittedView as? ViewGroup ?: break
            transformMatrixToChild(hittedView, transformMatrixOut)
        }

        println("hitted = ${view.className()}")
        return view
    }

    private fun transformMatrixToChild(child: View, matrix: Matrix) {
        matrix.preTranslate(child.x, child.y)
        matrix.preScale(child.scaleX, child.scaleY, child.pivotX, child.pivotY)
    }

    companion object {
        private val reusablePoint = Point()
        private val reusableRectF = RectF()
        private val reusableMatrix = Matrix()
        private val tmpMatrix = Matrix()
    }
}
