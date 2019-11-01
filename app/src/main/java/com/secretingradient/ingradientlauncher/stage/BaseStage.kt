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
    protected val reusablePoint = Point()

    protected fun getHitView(p: Point, view: ViewGroup = stageRootLayout, lastHitted: View? = null): View? {
        getLocationOnStage(view, reusablePoint)

        if (lastHitted != null && lastHitted.parent == view) {
            lastHitted.getHitRect(hitRect)
            if (hitRect.contains(p.x - reusablePoint.x, p.y - reusablePoint.y))
                return lastHitted
        }

        view.children.forEach {
            if (it != lastHitted) {
                it.getHitRect(hitRect)
                if (hitRect.contains(p.x - reusablePoint.x, p.y - reusablePoint.y)) {
                    return it
                }
            }
        }

        return null
    }

    protected fun getLocationOnStage(view: View, p: Point) {
        p.set(0,0)
        var v = view as View?
        // get location on stage
        while (v != stageRootLayout && v != null) {
            p.set(p.x + v.left, p.y + v.top)
            v = v.parent as View?
        }
        if (v == null) throw LauncherException("view $view must be a child of stageRootLayout")
    }
}