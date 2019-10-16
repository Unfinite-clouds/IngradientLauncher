package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.SnapElementInfo

abstract class BasePagerSnapStage(context: Context) : BaseStage(context), View.OnTouchListener {
    lateinit var stageViewPager: ViewPager2
    protected abstract val pagerAdapter: PagerSnapAdapter
    protected abstract val viewPagerId: Int

    override fun inflateAndAttach(stageRoot: StageRoot) {
        View.inflate(context, stageLayoutId, stageRoot)
        this.stageRoot = stageRoot
        stageViewPager = stageRoot.findViewById<ViewPager2>(viewPagerId)
        stageViewPager.adapter = pagerAdapter
    }

    // lazy page creating (now its bad)
    abstract inner class PagerSnapAdapter(val context: Context, val columnCount: Int, val rowCount: Int) : RecyclerView.Adapter<SnapLayoutHolder>() {
        val pageStates = mutableListOf<MutableList<SnapElementInfo>>()
        lateinit var currentViewHolder: SnapLayoutHolder

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapLayoutHolder {
            return SnapLayoutHolder(SnapLayout(context, columnCount*2, rowCount*2 ).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                setOnTouchListener(this@BasePagerSnapStage)
            })
        }

        override fun onBindViewHolder(holder: SnapLayoutHolder, page: Int) {
            // TODO: shit method
            // we don't need to removeAllViews and add them again, just rebind existing AppViews instead?
            // or NO? how to handle widgets?
            // we can use SnapLayoutState and preload all elements in one array like 'pageStates', so we will use only once SnapLayout
            // actually we should use RecyclerView with custom SnapLayoutManager and SnapHelper instead of ViewPager2 (ye, I love hard ways :)
            // (I don't know how to do page transition animations with RecyclerView)

            holder.snapLayout.removeAllViews()
            createPage(page)   // don't need to create pages lazy. create all in init() instead
            val pageState = pageStates[page]
            pageState.forEach {
                holder.snapLayout.addView(AppView(context, it.appInfo).apply { setOnTouchListener(this@BasePagerSnapStage) }, it.snapLayoutInfo) // we should avoid creating
            }
            currentViewHolder = holder
        }

        abstract fun createPage(page: Int): SnapLayout
    }

    class SnapLayoutHolder(val snapLayout: SnapLayout) : RecyclerView.ViewHolder(snapLayout)
}
