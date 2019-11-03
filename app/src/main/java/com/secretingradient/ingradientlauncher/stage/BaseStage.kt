package com.secretingradient.ingradientlauncher.stage

import android.graphics.Point
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.secretingradient.ingradientlauncher.LauncherException
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.element.AppView

abstract class BaseStage(val launcherRootLayout: LauncherRootLayout) {
    val context = launcherRootLayout.context
    protected abstract val stageLayoutId: Int
    lateinit var stageRootLayout: StageRootLayout

    open fun initInflate(stageRootLayout: StageRootLayout) {
        this.stageRootLayout = LayoutInflater.from(context).inflate(stageLayoutId, stageRootLayout, true) as StageRootLayout
        this.stageRootLayout.stage = this
    }

    open fun transferEvent(event: MotionEvent, v: AppView) {}

    open fun disallowScrollStage(disallow: Boolean = true) {
        stageRootLayout.parent.requestDisallowInterceptTouchEvent(disallow)
    }


    fun goToStage(number: Int) {
        launcherRootLayout.launcherViewPager.currentItem = number
    }

    private val hitRect = Rect()
    private val reusablePoint = Point()

    protected fun findViewAt(p: Point, lastHoveredView: View? = null, maxDepth: Int = 2): View? {
        if (lastHoveredView != null && lastHoveredView.parent != null) {
            getLocationOfViewGlobal(lastHoveredView, reusablePoint)

            lastHoveredView.getHitRect(hitRect)
            if (hitRect.contains(p.x - reusablePoint.x, p.y - reusablePoint.y))
                return lastHoveredView
        }

        var view: View = stageRootLayout
        var viewGroup: ViewGroup

        for (depth in 1..maxDepth) {
            viewGroup = view as? ViewGroup ?: break
            getLocationOfViewGlobal(viewGroup, reusablePoint)

            for (child in viewGroup.children) {
                if (child == lastHoveredView)
                    continue
                child.getHitRect(hitRect)
                if (hitRect.contains(p.x - reusablePoint.x, p.y - reusablePoint.y)) {
                    view = child
                    break
                }
            }
        }

        return if (view != stageRootLayout) view else null
    }

    fun getLocationOfViewGlobal(view: View, pointOut: Point) {
        pointOut.set(0,0)
        var v = view as View?
        // get location on stage
        while (v != stageRootLayout && v != null) {
            pointOut.set(pointOut.x + v.left, pointOut.y + v.top)
            v = v.parent as View?
        }
        if (v == null) throw LauncherException("view $view must be a child of stageRootLayout")
    }

    fun toLocationInView(globalPoint: Point, view: View, pointOut: Point) {
        getLocationOfViewGlobal(view, reusablePoint)
        pointOut.set(globalPoint.x - reusablePoint.x, globalPoint.y - reusablePoint.y)
    }
}