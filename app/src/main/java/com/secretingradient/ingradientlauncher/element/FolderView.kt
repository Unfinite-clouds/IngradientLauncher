package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.TextView

class FolderView(context: Context) : FrameLayout(context) {
    private val apps: MutableList<AppView> = mutableListOf()
    val debugText = TextView(context)

    init {
        setBackgroundColor(Color.YELLOW)
        debugText.text = apps.size.toString()
        addView(debugText)
    }

    fun addApps(vararg newApps: AppView) {
        addApps(newApps.asList())
    }

    fun addApps(newApps: Collection<AppView>) {
        apps.addAll(newApps)
        onUpdate()
    }

    operator fun get(i: Int) = apps[i]

    fun clear() {
        apps.clear()
        onUpdate()
    }

    private fun onUpdate() {
        debugText.text = apps.size.toString()
    }

}