package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView

class FolderView(context: Context, vararg newApps: AppView) : FrameLayout(context) {
    private val apps: MutableList<AppView> = mutableListOf()
    val debugText = TextView(context)
    val folderSize
        get() = apps.size
    var popupWindow: PopupWindow? = null

    init {
        setBackgroundColor(Color.YELLOW)
        debugText.text = apps.size.toString()
        debugText.textSize = 32f
        addView(debugText)
        setOnClickListener {
            openFolder()
        }
        addApps(newApps.asList())
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

    fun openFolder() {
        popupWindow = PopupWindow(context).apply {
            animationStyle = -1  // default
            this.contentView = this@FolderView
            showAsDropDown(this@FolderView)
        }
    }
}