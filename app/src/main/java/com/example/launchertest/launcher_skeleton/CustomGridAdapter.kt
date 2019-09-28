package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

abstract class CustomGridAdapter(val context: Context) : RecyclerView.Adapter<BoundViewHolder>() {
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

