package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.forEach
import com.secretingradient.ingradientlauncher.LauncherException
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.isElement

abstract class BaseStage(val launcherRootLayout: LauncherRootLayout) {
    val context: Context = launcherRootLayout.context
    val dataKeeper = launcherRootLayout.dataKeeper
    protected abstract val stageLayoutId: Int
    lateinit var stageRootLayout: StageRootLayout

    open fun initInflate(stageRootLayout: StageRootLayout) {
        this.stageRootLayout = LayoutInflater.from(context).inflate(stageLayoutId, stageRootLayout, true) as StageRootLayout
        this.stageRootLayout.stage = this
    }

    open fun onStageAttachedToWindow() {}

    open fun onDispatchDraw(canvas: Canvas?) {}

    open fun receiveTransferredElement(element: AppView) {} // todo make for any Element, not only for AppView

    open fun disallowVScroll(disallow: Boolean = true) {
        stageRootLayout.parent.requestDisallowInterceptTouchEvent(disallow)
    }

    private val hitRect = Rect()
    private val reusablePoint = Point()

    private fun hitTest(p: Point, view: View, computeParentLocationGlobal: Boolean = true): Boolean {
        if (view == stageRootLayout || view.parent == stageRootLayout)
            reusablePoint.set(0,0)
        else if (computeParentLocationGlobal)
            getLocationOfViewGlobal(view.parent as ViewGroup, reusablePoint)
        view.getHitRect(hitRect)
        return hitRect.contains(p.x - reusablePoint.x, p.y - reusablePoint.y)
    }

    fun findChildrenUnder(p: Point): MutableList<View> {
        val l = mutableListOf<View>()
        stageRootLayout.forEach {
            if (hitTest(p, it, false))
                l.add(it)
        }
        return l
    }

    fun findChildUnder(p: Point, lastHoveredView: View? = null): View? {
        if (lastHoveredView?.parent == stageRootLayout) {
            if (hitTest(p, lastHoveredView, false))
                return lastHoveredView
        }

        stageRootLayout.forEach {
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

        foundView = findInnerViewUnderInternal(p, stageRootLayout)

        return if (foundView != stageRootLayout) foundView else null
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
        while (v != stageRootLayout && v != null) {
            pointOut.set(pointOut.x + v.left, pointOut.y + v.top)
            v = v.parent as View?
        }
        if (v == null)
            throw LauncherException("view $view must be a child of stageRootLayout")
    }

    fun toLocationInView(globalPoint: Point, view: View, pointOut: Point) {
        getLocationOfViewGlobal(view, reusablePoint)
        pointOut.set(globalPoint.x - reusablePoint.x, globalPoint.y - reusablePoint.y)
    }
}