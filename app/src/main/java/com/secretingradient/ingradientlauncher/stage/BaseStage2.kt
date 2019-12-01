package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.secretingradient.ingradientlauncher.LauncherActivity
import com.secretingradient.ingradientlauncher.WallpaperFlow
import com.secretingradient.ingradientlauncher.drag.DragContext

abstract class BaseStage2(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    abstract val dragContext: DragContext?
    val launcherActivity = context as LauncherActivity
    val dataKeeper = launcherActivity.dataKeeper
    val scroller = WallpaperFlow.RecyclerScroller(launcherActivity.launcher)
    val gestureHelper = launcherActivity.gestureHelper

    open fun receiveTransferEvent(obj: Any?) {}

    fun disallowVScroll(disallow: Boolean = true) {
        parent.requestDisallowInterceptTouchEvent(disallow)
    }

/*    companion object {
        private val reusablePoint = Point()
        private val hitRect = Rect()
    }

    private fun hitTest(p: Point, view: View, computeParentLocationGlobal: Boolean = true): Boolean {
        if (view == contentView || view.parent == contentView)
            reusablePoint.set(0,0)
        else if (computeParentLocationGlobal)
            getLocationOfViewGlobal(view.parent as ViewGroup, reusablePoint)
        view.getHitRect(hitRect)
        return hitRect.contains(p.x - reusablePoint.x, p.y - reusablePoint.y)
    }

    fun findChildrenUnder(p: Point): MutableList<View> {
        val l = mutableListOf<View>()
        contentView.forEach {
            if (hitTest(p, it, false))
                l.add(it)
        }
        return l
    }

    fun findChildUnder(p: Point, lastHoveredView: View? = null): View? {
        if (lastHoveredView?.parent == contentView) {
            if (hitTest(p, lastHoveredView, false))
                return lastHoveredView
        }

        contentView.forEach {
            if (hitTest(p, it, false))
                return it
        }
        return null
    }

    fun findViewUnderInList(list: List<View>, p: Point, lastHoveredView: View? = null): View? {
        if (lastHoveredView?.parent is ViewGroup && lastHoveredView in list) {
            if (hitTest(p, lastHoveredView))
                return lastHoveredView
        }

        list.forEach {
            val parent = it.parent
            if (parent is ViewGroup) {
                if (hitTest(p, it))
                    return it
            }
        }

        return null
    }

    fun findInnerViewUnder(p: Point, lastHoveredView: View? = null): View? {
        var foundView: View? = null

        if (lastHoveredView != null)
            foundView = findInnerViewUnderInternal(p, lastHoveredView)

        if (foundView != null)
            return foundView

        foundView = findInnerViewUnderInternal(p, contentView)

        return if (foundView != contentView) foundView else null
    }

    private fun findInnerViewUnderInternal(p: Point, view: View): View? {
        // don't search in element's children. Suppose the Element is what we want to find

        if (view.parent == null)
            return null

        var foundView: View

        if (hitTest(p, view)) {
            foundView = view
        } else
            return null

        // view contains point, check children:
        var foundChild: View? = foundView
        while (foundChild is ViewGroup && !isElement(foundChild)) {
            // here foundChild == foundView
            foundChild = null
            getLocationOfViewGlobal(foundView, reusablePoint)
            for (child in (foundView as ViewGroup).children) {
                if (hitTest(p, child, false)) {
                    foundView = child
                    foundChild = child
                    break
                }
            }
        }
        return foundView
    }

    fun getLocationOfViewGlobal(view: View, pointOut: Point) {
        pointOut.set(0,0)
        var v = view as View?
        // get location on stage
        while (v != contentView && v != null) {
            pointOut.set(pointOut.x + v.x.toInt(), pointOut.y + v.y.toInt())
            v = v.parent as View?
        }
        if (v == null)
            throw LauncherException("view $view must be a subchild of stageRootLayout")
    }

    fun toLocationInView(globalPoint: Point, view: View, pointOut: Point) {
        getLocationOfViewGlobal(view, reusablePoint)
        pointOut.set(globalPoint.x - reusablePoint.x, globalPoint.y - reusablePoint.y)
    }*/
}