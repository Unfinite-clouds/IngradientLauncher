package com.example.launchertest

import android.content.ClipData
import android.content.Context
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.setPadding

class ScrollStage(context: Context) : BaseStage(context), View.OnLongClickListener, View.OnDragListener {
    var apps = AppManager.customGridApps
    val scrollId = R.id.main_stage_scroll
    val app_container = R.id.main_stage_app_container
    override val stageLayoutId = R.layout.stage_0_main_screen
    var widthCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_WIDTH_CELL, -1)
    var heightCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_HEIGHT_CELL, -1)

    override fun inflateAndAttach(rootLayout: ViewGroup) {
        super.inflateAndAttach(rootLayout)
        val container = rootLayout.findViewById<LinearLayout>(app_container)
        for (i in 0 until 10) {
            val appInfo = AppManager.getApp(apps[i]!!)
            if (appInfo != null)
                container.addView(AppShortcut(context, appInfo).apply {
                    setOnLongClickListener(this@ScrollStage)
                    setOnDragListener(this@ScrollStage)
                    layoutParams = LinearLayout.LayoutParams(widthCell,heightCell)
                    setPadding(0)
                })
        }
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppShortcut) {
            v.showPopupMenu()
            v.startDrag(ClipData.newPlainText("",""), v.createDragShadow(), Pair(v.parent as DummyCell, v), 0)
        }
        return true
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        return true
    }
}