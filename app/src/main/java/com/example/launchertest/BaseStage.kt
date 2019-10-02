package com.example.launchertest

import android.content.Context
import android.view.View
import android.view.ViewGroup

abstract class BaseStage(val context: Context) {
    val launcherViewPager = (context as MainActivity).launcherViewPager
    protected abstract val stageLayoutId: Int

    open fun inflateAndAttach(rootLayout: ViewGroup) {
        View.inflate(context, stageLayoutId, rootLayout)
    }
}