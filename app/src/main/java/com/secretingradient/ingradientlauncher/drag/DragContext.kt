package com.secretingradient.ingradientlauncher.drag

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.secretingradient.ingradientlauncher.LauncherException
import com.secretingradient.ingradientlauncher.className

abstract class DragContext {
    abstract var canStartDrag: Boolean
    abstract val contentView: ViewGroup
    fun toPointLocal(pointGlobal: IntArray) {
        contentView.getLocationOnScreen(_reusablePoint)
        pointGlobal[0] -= _reusablePoint[0]
        pointGlobal[1] -= _reusablePoint[1]
    }
    fun getDraggableUnder(pointLocal: IntArray): Draggable? {
        if (!canStartDrag)
            return null
        return hitTraversal(pointLocal) as? Draggable

    }
    fun getHoverableUnder(pointLocal: IntArray): Hoverable? {
        return hitTraversal(pointLocal) as? Hoverable
    }
    private fun hitView(v: View, pointLocal: IntArray): Boolean {
        getLocationIn(contentView, v, _reusablePoint)
        _reusableRect.set(_reusablePoint[0], _reusablePoint[1], _reusablePoint[0] + v.width, _reusablePoint[1] + v.height)
        return _reusableRect.contains(pointLocal[0], pointLocal[1])
    }
    private fun getLocationIn(viewGroup: ViewGroup, v: View, pointOut: IntArray) {
        if (!v.isAttachedToWindow) throw LauncherException("${v.className()} isn't attached")
        pointOut[0] = 0
        pointOut[1] = 0
        var parent: View = v
        while (parent != viewGroup) {
            pointOut[0] += parent.x.toInt()
            pointOut[1] += parent.y.toInt()
            parent = parent.parent as? ViewGroup ?: throw LauncherException("view ${v.className()} must be a subchild of viewGroup ${viewGroup.className()}")
        }
//        println("getLocationIn result: ${pointOut.joinToString(", ")}")
    }
    private fun hitTraversal(pointLocal: IntArray) : View? {
        var view: View = if (hitView(contentView, pointLocal)) contentView else return null
            .also { println("hitTraversal result: $it") }
        var hittedView: View?
        var nextParent: ViewGroup? = contentView

        while (nextParent is ViewGroup) {
            hittedView = null
            for (child in nextParent.children) {
                if (hitView(child, pointLocal)) {
                    hittedView = child
                    view = child
                    break
                }
            }
            nextParent = hittedView as? ViewGroup
        }

        return view
//            .also { println("hitTraversal result: $it") }
    }
    companion object {
        private val _reusableRect = Rect()
        private val _reusablePoint = IntArray(2)
    }
}
