package com.secretingradient.ingradientlauncher.stage

import android.graphics.Point
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.secretingradient.ingradientlauncher.LauncherException
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.isElement

abstract class BaseStage(val launcherRootLayout: LauncherRootLayout) {
    val context = launcherRootLayout.context
    protected abstract val stageLayoutId: Int
    lateinit var stageRootLayout: StageRootLayout

    open fun initInflate(stageRootLayout: StageRootLayout) {
        this.stageRootLayout = LayoutInflater.from(context).inflate(stageLayoutId, stageRootLayout, true) as StageRootLayout
        this.stageRootLayout.stage = this
    }

    open fun receiveTransferredElement(element: AppView) {} // todo make for any Element, not only for AppView

    open fun disallowVScroll(disallow: Boolean = true) {
        stageRootLayout.parent.requestDisallowInterceptTouchEvent(disallow)
    }


    fun goToStage(number: Int) {
        launcherRootLayout.launcherViewPager.currentItem = number
    }

    private val hitRect = Rect()
    private val reusablePoint = Point()

    protected fun findViewUnder(p: Point, lastHoveredView: View? = null): View? {
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

        getLocationOfViewGlobal(view, reusablePoint)
        view.getHitRect(hitRect)
        if (hitRect.contains(p.x - reusablePoint.x, p.y - reusablePoint.y)) {
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
                child.getHitRect(hitRect)
                if (hitRect.contains(p.x - reusablePoint.x, p.y - reusablePoint.y)) {
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