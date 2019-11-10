package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.secretingradient.ingradientlauncher.data.AppInfo
import com.secretingradient.ingradientlauncher.data.FolderInfo

class FolderView : FrameLayout {
    var info: FolderInfo = FolderInfo(mutableListOf())
    val debugText = TextView(context)
    val folderSize
        get() = info.apps.size

    init {
        setBackgroundColor(Color.YELLOW)
        debugText.text = info.apps.size.toString()
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
        info.apps.addAll(newApps)
        onUpdate()
    }

    fun getApp(i: Int) = info.apps[i]

    fun getApps() = info.apps

    fun clear() {
        info.apps.clear()
        onUpdate()
    }

    private fun onUpdate() {
        debugText.text = info.apps.size.toString()
    }
}