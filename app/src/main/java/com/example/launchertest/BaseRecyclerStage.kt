package com.example.launchertest

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

abstract class BaseRecyclerStage(context: Context) : BaseStage(context) {
    lateinit var stageViewPager: ViewPager2
    protected abstract val stageAdapter: StageAdapter
    protected abstract val viewPagerId: Int

    override fun inflateAndAttach(rootLayout: ViewGroup) {
        View.inflate(context, stageLayoutId, rootLayout)
        stageViewPager = rootLayout.findViewById<ViewPager2>(viewPagerId)
        stageViewPager.adapter = stageAdapter
    }

    // lazy creating pages
    abstract class StageAdapter(val context: Context) : RecyclerView.Adapter<BoundViewHolder>() {
        val pages = mutableListOf<LauncherPageGrid>()

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
            if (pages.lastIndex <= page)
                pages.add(page, createPage(context, page))
            val VHRootView = (holder.itemView as ViewGroup)
            (pages[page].parent as? ViewGroup)?.removeAllViews()
            holder.itemView.removeAllViews()
            VHRootView.addView(pages[page])
        }

        abstract fun createPage(context: Context, page: Int): LauncherPageGrid
    }
}
