package com.example.launchertest

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup

abstract class BaseStage(val context: Context) {
    val launcherViewPager = (context as MainActivity).launcherViewPager
    protected abstract val stageLayoutId: Int

    open fun inflateAndAttach(rootLayout: ViewGroup) {
        LayoutInflater.from(context).inflate(stageLayoutId, rootLayout, true)
    }
}