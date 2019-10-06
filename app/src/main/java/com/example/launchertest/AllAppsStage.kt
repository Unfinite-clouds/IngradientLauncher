package com.example.launchertest

import android.content.ClipData
import android.content.Context
import android.view.DragEvent
import android.view.View
import androidx.core.view.setPadding
import kotlin.math.ceil

class AllAppsStage(context: Context) : BasePagerStage(context), View.OnLongClickListener {
    var allApps = AppManager.getSortedApps()
    var rowCount = getPrefs(context).getInt(Preferences.ALLAPPS_ROW_COUNT, -1)
    var columnCount = getPrefs(context).getInt(Preferences.ALLAPPS_COLUMN_COUNT, -1)
    var pageCount = ceil(allApps.size.toFloat() / (rowCount*columnCount)).toInt()
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_2_all_apps
    override val viewPagerId = R.id.all_apps_vp
    override val stageAdapter = AllAppsAdapter(context) as StageAdapter

    inner class AllAppsAdapter(context: Context) : BasePagerStage.StageAdapter(context) {
        override fun getItemCount() = pageCount

        override fun createPage(context: Context, page: Int): LauncherPageGrid {
            val grid = LauncherPageGrid(context, rowCount, columnCount, page, this@AllAppsStage)

            var position: Int
            for (i in 0 until grid.size) {
                position = i+grid.size*page
                if (position > allApps.size - 1)
                    break

                val appInfo = AppManager.getApp(allApps[position])
                if (appInfo != null)
                    grid.addShortcut(createAppShortcut(appInfo), position)
            }
            return grid
        }

    }

    fun createAppShortcut(appInfo: AppInfo): AppShortcut {
        return AppShortcut(context, appInfo).apply { adaptApp(this) }
    }

    override fun adaptApp(app: AppShortcut) {
        app.setOnLongClickListener(this@AllAppsStage)
        app.setPadding(cellPadding)
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppShortcut) {
            startDrag(v)
        }
        return true
    }

    override fun startDrag(v: View) {
        if (v is AppShortcut) {
            v.startDrag(ClipData.newPlainText("",""), v.createDragShadow(), Pair(null, v), 0)
        }
    }

    override fun onFocus(event: DragEvent) {
        flipToStage(1, event)
    }

    override fun onFocusLost(event: DragEvent) {
    }

    override fun onDragEnded() {
    }

}