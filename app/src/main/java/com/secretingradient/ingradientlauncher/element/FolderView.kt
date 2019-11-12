package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.data.*

class FolderView : FrameLayout {
    var info: FolderInfo = FolderInfo(mutableListOf())
    val debugText = TextView(context)
    val folderSize
        get() = info.apps.size
    val apps: MutableList<AppInfo>
        get() = info.apps

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
        update()
    }

    fun removeApp(i: Int) {
        info.apps.removeAt(i)
        update()
    }

    fun clear() {
        info.apps.clear()
        update()
    }

    fun update() {
        debugText.text = info.apps.size.toString()
    }

    fun revert(dataset: Dataset<Data, Info>) {
        val pos = (this.layoutParams as SnapLayout.SnapLayoutParams).position
        val p = parent as? SnapLayout ?: return
        p.removeView(this)
        val app = apps[0].createView(context)
        p.addNewView(app, pos, 2, 2)
        dataset.put(pos, app.info!!, true)
    }
}