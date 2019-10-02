package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.*

class StageAdapter(val context: Context) : RecyclerView.Adapter<BoundViewHolder>() {
    val stages = mutableListOf<ViewGroup>()

    override fun getItemCount(): Int = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoundViewHolder {
        return BoundViewHolder(LayoutInflater.from(context).inflate(R.layout.stage, parent, false))
    }

    override fun onBindViewHolder(holder: BoundViewHolder, position: Int) {
        if (holder.boundPosition != position) {
            println("binding position $position")
            val rootLayout = holder.itemView as ViewGroup
            rootLayout.removeAllViews()
            val stage = createStage(context, position)
            stage.inflateAndAttach(rootLayout)
            holder.boundPosition = position
        }
    }

    private fun createStage(context: Context, position: Int) : BaseStage {
        val stage: BaseStage
        when (position) {
            0 -> {
                // Scroll stage
/*                View.inflateAndAttach(context, R.layout.stage_0_main_screen, rootStage)
                for (i in 0..10) {
                    //fill first 10 apps
                    rootStage.iconContainer.addView(
                        IconFactory(
                            context,
                            getPrefs(context).getInt(Preferences.MAIN_SCREEN_ICONS_COUNT, -1))
                            .createIcon(AppManager.getApp(AppManager.getSortedApps()[i])!!)
                    )
                }*/
                stage = AllAppsStage(context)
            }
            1 -> {
                // Custom stage
/*                View.inflateAndAttach(context, R.layout.stage_1_custom_grid, rootStage)
                rootStage.custom_grid_vp.adapter = object : CustomGridAdapter(context) {
                    override fun createGrid(context: Context, page: Int): LauncherScreenGrid {
                        return createCustomGrid(context, page)
                    }
                }
                (context as MainActivity).stageCustomGrid = rootStage.custom_grid_vp*/
                stage = AllAppsStage(context)
            }
            2 -> {
                // AllApps stage
                stage = AllAppsStage(context)
            }
            3 -> {
                stage = AllAppsStage(context)
            }
            else -> throw LauncherException("position must be in range 0..${itemCount - 1}")
        }
        return stage
    }
}

fun createCustomGrid(context: Context, page: Int): LauncherScreenGrid {
    val grid = LauncherScreenGrid(context, 5, 4, page).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    grid.forEach {
        it.setOnDragListener(DragCustomGrid())
    }

    val customGridAppsApps = AppManager.customGridApps
    var appInfo: AppInfo?

    customGridAppsApps.forEach {
        if (it.key in grid.gridBounds) {
            appInfo = AppManager.getApp(it.value)
            if (appInfo != null) grid.addShortcut(AppShortcut(context, appInfo!!).apply { setOnLongClickListener(this) }, it.key)
        }
    }

    return grid
}