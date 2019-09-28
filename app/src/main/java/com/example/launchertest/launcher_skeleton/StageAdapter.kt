package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.*
import kotlinx.android.synthetic.main.stage_0_main_screen.view.*
import kotlinx.android.synthetic.main.stage_1_custom_grid.view.*
import kotlinx.android.synthetic.main.stage_2_all_apps.view.*

class StageAdapter(val context: Context) : RecyclerView.Adapter<BoundViewHolder>() {
    val stages = mutableListOf<ViewGroup>()

    override fun getItemCount(): Int = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoundViewHolder {
        return BoundViewHolder(LayoutInflater.from(context).inflate(R.layout.stage, parent, false))
    }

    override fun onBindViewHolder(holder: BoundViewHolder, position: Int) {
        if (holder.boundPosition != position) {
            println("binding position $position")
            val stage = holder.itemView as ViewGroup
            stage.removeAllViews()
            bindStage(context, stage, position)
            holder.boundPosition = position
        }
    }

    private fun bindStage(context: Context, stageRoot: ViewGroup, position: Int) {
        when (position) {
            0 -> {
                // Scroll stage
                View.inflate(context, R.layout.stage_0_main_screen, stageRoot)
                for (i in 0..10) {
                    //fill first 10 apps
                    stageRoot.iconContainer.addView(
                        IconFactory(
                            context,
                            getPrefs(context).getInt(Preferences.MAIN_SCREEN_ICONS_COUNT, -1))
                            .createIcon(AppManager.getApp(AppManager.getSortedApps()[i])!!)
                    )
                }
            }
            1 -> {
                // Custom stage
                View.inflate(context, R.layout.stage_1_custom_grid, stageRoot)
                stageRoot.custom_grid_vp.adapter = object : CustomGridAdapter(context) {
                    override fun createGrid(context: Context, page: Int): LauncherScreenGrid {
                        return createCustomGrid(context, page)
                    }
                }
                (context as MainActivity).stageCustomGrid = stageRoot.custom_grid_vp
            }
            2 -> {
                // AllApps stage
                View.inflate(context, R.layout.stage_2_all_apps, stageRoot)
                stageRoot.all_apps_vp.adapter = object : CustomGridAdapter(context) {
                    override fun createGrid(context: Context, page: Int): LauncherScreenGrid {
                        return createAllAppsGrid(context, page)
                    }
                }
            }
        }
        stageRoot.setBackgroundResource(R.color.Vignette)
    }
}

class BoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var boundPosition = -1
}

fun createCustomGrid(context: Context, page: Int): LauncherScreenGrid {
    val grid = LauncherScreenGrid(context, 5, 4, page).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    val customGridAppsApps = AppManager.customGridApps
    var appInfo: AppInfo?

    customGridAppsApps.forEach {
        if (it.value in grid.gridBounds) {
            appInfo = AppManager.getApp(it.key)
            if (appInfo != null) grid.addViewTo(AppShortcut(context, appInfo!!), it.value)
        }
    }

    return grid
}

fun createAllAppsGrid(context: Context, page: Int): LauncherScreenGrid {
    val grid = LauncherScreenGrid(context, 5, 4, page).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    val allApps = AppManager.getSortedApps()
    var position: Int

    for (i in 0 until grid.size) {
        position = i+grid.size*page
        if (position > allApps.size - 1)
            break

        val appInfo = AppManager.getApp(allApps[position])
        if (appInfo != null)
            grid.addViewTo(AppShortcut(context, appInfo), position)
    }

    grid.setBackgroundColor(randomColor())

    return grid
}