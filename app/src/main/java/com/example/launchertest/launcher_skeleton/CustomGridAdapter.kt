package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.AppManager

class CustomGridAdapter : RecyclerView.Adapter<CustomScreenHolder>() {

    private lateinit var context: Context

    companion object {
        val colors = intArrayOf(
            android.R.color.holo_red_light,
            android.R.color.black,
            android.R.color.holo_blue_dark,
            android.R.color.holo_purple,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.transparent
        )
        val grids = mutableListOf<LauncherScreenGrid>()
    }

    override fun getItemCount(): Int = 20

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomScreenHolder {
        context = parent.context

//        return CustomScreenHolder(context, LauncherScreenGrid(context, 5, 4).apply {
//            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//        })

        return CustomScreenHolder(context, LinearLayout(context).apply { layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        })
    }


    override fun onBindViewHolder(holder: CustomScreenHolder, page: Int) {
        if (grids.lastIndex <= page)
            grids.add(page, bind(context, page))
        val VHRootView = (holder.itemView as ViewGroup)
        (grids[page].parent as? ViewGroup)?.removeAllViews()
        holder.itemView.removeAllViews()
        VHRootView.addView(grids[page])
    }
}


class CustomScreenHolder(private val context: Context, grid: ViewGroup) : RecyclerView.ViewHolder(grid) {
//    var bindedPos = -1
//    val width = grid.columnCount
//    val height = grid.rowCount
}

fun bind(context: Context, page: Int): LauncherScreenGrid {
    println("loading page $page")
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