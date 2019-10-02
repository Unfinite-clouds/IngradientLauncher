package com.example.launchertest

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout

class ScrollStage(context: Context) : BaseStage(context) {
    var apps = AppManager.customGridApps
    val scrollId = R.id.main_stage_scroll
    val app_container = R.id.main_stage_app_container
    override val stageLayoutId = R.layout.stage_0_main_screen

    override fun inflateAndAttach(rootLayout: ViewGroup) {
        super.inflateAndAttach(rootLayout)
        val container = rootLayout.findViewById<LinearLayout>(app_container)
        for (i in 0 until 10) {
            val appInfo = AppManager.getApp(apps[i]!!)
            if (appInfo != null)
                container.addView(AppShortcut(context, appInfo).apply {
                    setOnLongClickListener(CustomGridStage)
                    layoutParams = LinearLayout.LayoutParams(-2,-2,1f)
                })
        }
    }
}