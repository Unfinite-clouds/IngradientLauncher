package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.ViewGroup
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
    }

    override fun getItemCount(): Int = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomScreenHolder {
        context = parent.context

        return CustomScreenHolder(context, LauncherScreenGrid(context, 5, 4).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        })
    }


    override fun onBindViewHolder(screenHolder: CustomScreenHolder, page: Int) {
        if (screenHolder.adapterPosition != page || screenHolder.layoutPosition != page || screenHolder.adapterPosition != screenHolder.layoutPosition)
            println("Bind with bug APos: ${screenHolder.adapterPosition}, LPos: ${screenHolder.layoutPosition}")
        screenHolder.bind(page)
    }
}


class CustomScreenHolder(private val context: Context, val grid: LauncherScreenGrid) : RecyclerView.ViewHolder(grid), LauncherScreenGrid.OnDropListener {
    var bindedPos = -1
    val width = grid.columnCount
    val height = grid.rowCount

    fun bind(page: Int) {
        if (bindedPos != page) {

            grid.clearGrid()
            grid.addOnDropListener(this)

            val customGridAppsApps = AppManager.customGridApps
            var appInfo: AppInfo?

            customGridAppsApps.forEach {
                if (it.value in width*height*page until width*height*(page+1)) {
                    appInfo = AppManager.getApp(it.key)
                    if (appInfo != null) grid.addViewTo(AppShortcut(context, appInfo!!).apply { setOnLongClickListener(grid) }, it.value%width, it.value/width)
                }
            }

            bindedPos = page
        }
    }

    override fun onDrop(appInfo: AppInfo, newPos: Int, oldPos: Int) {
        AppManager.applyCustomGridChanges(context, appInfo.id, newPos)
    }
}