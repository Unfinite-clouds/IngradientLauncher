package com.example.launchertest

import android.content.ClipData
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.launchertest.launcher_skeleton.AppShortcut
import com.example.launchertest.launcher_skeleton.BoundViewHolder
import com.example.launchertest.launcher_skeleton.LauncherScreenGrid
import kotlinx.android.synthetic.main.stage_2_all_apps.view.*

class AllAppsStage(val context: Context, private val stageRoot: ViewGroup) : View.OnLongClickListener {
    private val mainRoot = (context as MainActivity).stages
    private val stageViewPager: ViewPager2
    private val stageAdapter: RecyclerView.Adapter<*>

    init {
        val stage = View.inflate(context, R.layout.stage_2_all_apps, stageRoot)
        stageViewPager = stage.all_apps_vp
        stageAdapter = AllAppsAdapter()
        stageViewPager.adapter = stageAdapter
    }

    class AllAppsAdapter : RecyclerView.Adapter<BoundViewHolder>() {
        lateinit var context: Context
        val grids = mutableListOf<LauncherScreenGrid>()

        override fun getItemCount(): Int = 3

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoundViewHolder {
            context = parent.context
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
                grids.add(page, createAllAppsGrid(context, page))
            val VHRootView = (holder.itemView as ViewGroup)
            (grids[page].parent as? ViewGroup)?.removeAllViews()
            holder.itemView.removeAllViews()
            VHRootView.addView(grids[page])
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
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppShortcut) {
            mainRoot.currentItem = 1
            val newShortcut = AppShortcut(context, v.appInfo)
            newShortcut.setOnLongClickListener(newShortcut)
            val dragShadow = v.createDragShadow()
            v.startDrag(ClipData.newPlainText("",""), dragShadow, Pair(null, newShortcut), 0)
        }
        return true
    }

    fun loadData() {

    }

    fun saveData() {

    }

    // fun onDrag...

}