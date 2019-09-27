package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.AppManager
import com.example.launchertest.randomColor

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

    override fun getItemCount(): Int = 20

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomScreenHolder {
        context = parent.context

        return CustomScreenHolder(context, LauncherScreenGrid(context, 5, 4).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        })
    }


    override fun onBindViewHolder(screenHolder: CustomScreenHolder, page: Int) {
        screenHolder.bind(page)
    }

//    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//        super.onAttachedToRecyclerView(recyclerView)
//    }
//
//    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
//        super.onDetachedFromRecyclerView(recyclerView)
//    }

    override fun onViewAttachedToWindow(holder: CustomScreenHolder) {
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: CustomScreenHolder) {
        super.onViewDetachedFromWindow(holder)
    }

    override fun onViewRecycled(holder: CustomScreenHolder) {
        super.onViewRecycled(holder)
    }
}


class CustomScreenHolder(private val context: Context, val grid: LauncherScreenGrid) : RecyclerView.ViewHolder(grid) {
    var bindedPos = -1
    val width = grid.columnCount
    val height = grid.rowCount

    fun bind(page: Int) {
        if (bindedPos != page) {
            println("loading page $page")
            grid.clearGrid()
            grid.page = page

            val customGridAppsApps = AppManager.customGridApps
            var appInfo: AppInfo?

            customGridAppsApps.forEach {
                if (it.value in grid.gridBounds) {
                    appInfo = AppManager.getApp(it.key)
                    if (appInfo != null) grid.addViewTo(AppShortcut(context, appInfo!!), it.value)
                }
            }

            bindedPos = page
            grid.setBackgroundColor(randomColor())
        }
    }

}