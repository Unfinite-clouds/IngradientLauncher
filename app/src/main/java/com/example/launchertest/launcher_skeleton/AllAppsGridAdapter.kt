package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.AppManager
import com.example.launchertest.randomColor

@Deprecated("")
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


    override fun onBindViewHolder(screenHolder: ScreenHolder, page: Int) {
        screenHolder.bind(page)
    }
}


@Deprecated("")
class ScreenHolder(private val context: Context, val grid: LauncherScreenGrid) : RecyclerView.ViewHolder(grid) {
    var bindedPos = -1
    val width = grid.columnCount
    val height = grid.rowCount

    fun bind(page: Int) {
        if (bindedPos != page) {

            grid.clearGrid()
            grid.page = page

            val allApps = AppManager.getSortedApps()
            var position: Int
            for (i in 0 until width*height) {
                position = i+width*height*page
                if (position > allApps.size - 1)
                    break
                val appInfo = AppManager.getApp(allApps[position])

                if (appInfo != null)
                    grid.addViewTo(AppShortcut(context, appInfo), position)
            }

            grid.setBackgroundColor(randomColor())
            bindedPos = page
        }
    }
}