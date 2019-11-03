package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.PopupWindow
import android.widget.TextView
import kotlin.math.ceil
import kotlin.math.sqrt

class FolderView(context: Context, vararg newApps: AppView) : FrameLayout(context) {
    private val apps: MutableList<AppView> = mutableListOf()
    val debugText = TextView(context)
    val folderSize
        get() = apps.size
    val popupWindow: PopupWindow

    init {
        setBackgroundColor(Color.YELLOW)
        debugText.text = apps.size.toString()
        debugText.textSize = 32f
        addView(debugText)
        setOnClickListener {
            getPopupFolder()
        }
        addApps(newApps.asList())
        popupWindow = PopupWindow(context).apply {
            animationStyle = -1  // default
            contentView = GridLayout(context)
        }
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

    fun getPopupFolder(): PopupWindow {
        val p = IntArray(2)
        this.getLocationOnScreen(p)
        val grid = popupWindow.contentView as GridLayout
        var n = ceil(sqrt(apps.size.toFloat())).toInt()
        if (n == 1) n = 2
        else if (n == 2 && apps.size == 4) n = 3
        grid.removeAllViews()
        grid.columnCount = n; grid.rowCount = n
        apps.forEachIndexed {i, appView ->
            val x = i % n
            val y = i / n
            grid.addView(appView, GridLayout.LayoutParams(GridLayout.spec(y), GridLayout.spec(x)))
        }
        return popupWindow
    }
}