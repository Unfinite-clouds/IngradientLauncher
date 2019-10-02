package com.example.launchertest

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.launchertest.launcher_skeleton.AppShortcut
import com.example.launchertest.launcher_skeleton.LauncherScreenGrid

abstract class BaseStage(val context: Context) : View.OnLongClickListener {
    protected val mainRoot = (context as MainActivity).stages
    protected lateinit var stageViewPager: ViewPager2
    protected lateinit var stageAdapter: RecyclerView.Adapter<*>

    init {

    }

    fun inflate(stageRoot: ViewGroup, stageLayout: Int, viewPagerId: Int) {
        val stage = View.inflate(context, stageLayout, stageRoot)
        stageViewPager = stage.findViewById<ViewPager2>(viewPagerId)
    }

    abstract override fun onLongClick(v: View?): Boolean

    fun loadData() {

    }

    fun saveData() {

    }

    // fun onDrag...

}


abstract class StageAdapter(val context: Context) : RecyclerView.Adapter<BoundViewHolder>() {
    val grids = mutableListOf<LauncherScreenGrid>()

    override fun getItemCount(): Int = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoundViewHolder {
        return BoundViewHolder(LinearLayout(context).apply {
            layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
        })
    }

    override fun onBindViewHolder(holder: BoundViewHolder, page: Int) {
        if (grids.lastIndex <= page)
            grids.add(page, createGrid(context, page))
        val VHRootView = (holder.itemView as ViewGroup)
        (grids[page].parent as? ViewGroup)?.removeAllViews()
        holder.itemView.removeAllViews()
        VHRootView.addView(grids[page])
    }

    abstract fun createGrid(context: Context, page: Int): LauncherScreenGrid
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
            grid.addShortcut(AppShortcut(context, appInfo).apply { setOnLongClickListener(context as MainActivity) }, position)
    }

    return grid
}

class BoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var boundPosition = -1
}