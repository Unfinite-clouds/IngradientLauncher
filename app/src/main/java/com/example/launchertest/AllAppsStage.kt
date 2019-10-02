package com.example.launchertest

import android.content.ClipData
import android.content.Context
import android.view.View
import com.example.launchertest.launcher_skeleton.AppShortcut

class AllAppsStage(context: Context) : BaseStage(context) {

    override fun onLongClick(v: View?): Boolean {
        if (v is AppShortcut) {
            mainRoot.currentItem = 1
            val newShortcut = AppShortcut(context, v.appInfo)
            newShortcut.setOnLongClickListener(newShortcut)
            val dragShadow = v.createDragShadow()
            v.startDrag(ClipData.newPlainText("",""), dragShadow, Pair(null, newShortcut), 0)
        }
        return true
    }


    // fun onDrag...

}