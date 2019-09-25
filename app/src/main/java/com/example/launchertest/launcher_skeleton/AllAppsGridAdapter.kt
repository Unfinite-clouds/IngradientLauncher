package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.AppManager
import com.example.launchertest.randomColor

class AllAppsGridAdapter : RecyclerView.Adapter<ScreenHolder>() {

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

    override fun getItemCount(): Int = colors.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenHolder {
        context = parent.context

        return ScreenHolder(context, LauncherScreenGrid(context, 4, 3).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        })
    }


    override fun onBindViewHolder(screenHolder: ScreenHolder, position: Int) {
        if (screenHolder.adapterPosition != position || screenHolder.layoutPosition != position || screenHolder.adapterPosition != screenHolder.layoutPosition)
            println("Bind with bug APos: ${screenHolder.adapterPosition}, LPos: ${screenHolder.layoutPosition}")
        screenHolder.bind(position)
    }
}


class ScreenHolder(private val context: Context, val grid: LauncherScreenGrid) : RecyclerView.ViewHolder(grid) {
    var bindedPos = -1
    val width = grid.columnCount
    val height = grid.rowCount

    fun bind(position: Int) {
        if (bindedPos != position) {

            grid.clearGrid()

            val allApps = AppManager.getSortedApps()
            var app: Int
            for (i in 0 until width*height) {
                app = i+width*height*position
                if (app > allApps.size - 1)
                    break
                val appInfo = AppManager.getApp(allApps[app])

                if (appInfo != null)
                    grid.addViewTo(AppShortcut(context, appInfo).apply { setOnLongClickListener(grid) }, i%width, i/width)
            }

            grid.setBackgroundColor(randomColor())
            bindedPos = position
        }
    }
}