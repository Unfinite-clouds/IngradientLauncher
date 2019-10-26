package com.secretingradient.ingradientlauncher.stage

import android.view.LayoutInflater
import android.view.MotionEvent
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
}