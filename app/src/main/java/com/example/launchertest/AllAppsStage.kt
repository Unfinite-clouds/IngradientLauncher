package com.example.launchertest

import android.content.ClipData
import android.content.Context
import android.view.View
import kotlin.math.ceil

class AllAppsStage(context: Context) : BaseRecyclerStage(context), View.OnLongClickListener {
    var allApps = AppManager.getSortedApps()
    var rowCount = getPrefs(context).getInt(Preferences.ALLAPPS_ROW_COUNT, -1)
    var columnCount = getPrefs(context).getInt(Preferences.ALLAPPS_COLUMN_COUNT, -1)
    var pageCount = ceil(allApps.size.toFloat() / (rowCount*columnCount)).toInt()
    override val stageLayoutId = R.layout.stage_2_all_apps
    override val viewPagerId = R.id.all_apps_vp
    override val stageAdapter = AllAppsAdapter(context) as StageAdapter

    inner class AllAppsAdapter(context: Context) : BaseRecyclerStage.StageAdapter(context) {
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
                    grid.addShortcut(AppShortcut(context, appInfo).apply { setOnLongClickListener(this@AllAppsStage) }, position)
            }
            return grid
        }

    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppShortcut) {
            launcherViewPager.currentItem = 1
            val newShortcut = AppShortcut(context, v.appInfo)
            newShortcut.setOnLongClickListener(CustomGridStage)
            val dragShadow = v.createDragShadow()
            v.startDrag(ClipData.newPlainText("",""), dragShadow, Pair(null, newShortcut), 0)
        }
        return true
    }

}