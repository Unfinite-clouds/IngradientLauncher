package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.secretingradient.ingradientlauncher.data.AppInfo

class FolderView : FrameLayout {
    private val apps: MutableList<AppInfo> = mutableListOf()
    val debugText = TextView(context)
    val folderSize
        get() = apps.size

    init {
        setBackgroundColor(Color.YELLOW)
        debugText.text = apps.size.toString()
        debugText.textSize = 32f
        debugText.setTextColor(Color.BLACK)
        addView(debugText)
    }

    constructor(context: Context, vararg apps: AppInfo) : super(context){
        addApps(apps.asList())
    }
    constructor(context: Context, apps: Collection<AppInfo>) : super(context){
        addApps(apps)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun addApps(vararg newApps: AppInfo) {
        addApps(newApps.asList())
    }

    fun addApps(newApps: Collection<AppInfo>) {
        apps.addAll(newApps)
        onUpdate()
    }

    fun getApp(i: Int) = apps[i]

    fun getApps() = apps

    fun clear() {
        apps.clear()
        onUpdate()
    }

    private fun onUpdate() {
        debugText.text = apps.size.toString()
    }
}