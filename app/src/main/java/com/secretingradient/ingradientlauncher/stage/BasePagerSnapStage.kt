package com.secretingradient.ingradientlauncher.stage

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.data.Dataset
import com.secretingradient.ingradientlauncher.setSnapLayoutParams

abstract class BasePagerSnapStage(launcherRootLayout: LauncherRootLayout) : BaseStage(launcherRootLayout), View.OnTouchListener {
    lateinit var stageVP: ViewPager2
    val stageRV: RecyclerView
        get() = stageVP.getChildAt(0) as RecyclerView
    protected abstract val pagerAdapter: PagerSnapAdapter
    protected abstract val viewPagerId: Int
    abstract override val stageLayoutId: Int
    abstract var columnCount: Int
    abstract var rowCount: Int
    abstract var pageCount: Int
    abstract val dataset: Dataset<*, *>

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        stageVP = stageRootLayout.findViewById(viewPagerId)
        stageVP.adapter = pagerAdapter
    }

    // lazy page creating is bad
    inner class PagerSnapAdapter : RecyclerView.Adapter<SnapLayoutHolder>() {
        private val pageSize = columnCount*rowCount*2
        lateinit var currentViewHolder: SnapLayoutHolder

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapLayoutHolder {
            return SnapLayoutHolder(SnapLayout(context, columnCount*2, rowCount*2 ).apply {
                layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
//                setOnTouchListener(this@BasePagerSnapStage)
            })
        }

        override fun onBindViewHolder(holder: SnapLayoutHolder, page: Int) {
            // TODO: shit method
            // we don't need to removeAllViews and add them again, just rebind existing AppViews instead?
            // or NO? how to handle widgets?
            // we can use SnapLayoutState and preload all elements in one array like 'pageStates', so we will use only once SnapLayout
            // actually we should use RecyclerView with custom SnapLayoutManager and SnapHelper instead of ViewPager2 (ye, I love hard ways :)
            // (I don't know how to do page transition animations with RecyclerView)

/*
            pageStates.add(createPage(page)) // don't need to create pages lazy. create all in init() instead
            val pageState = pageStates[page]
*/
            holder.snapLayout.removeAllViews()




            // WTF why all AppViews is null??
            dataset.forEach {
                if (isPosInPage(it.key, page)) {
                    holder.snapLayout.addView(it.value.createView(context) // avoid creating here
                        .apply { setSnapLayoutParams(it.key)}) // bad way
                }
            }

            currentViewHolder = holder
        }

        override fun getItemCount(): Int = pageCount

        private fun isPosInPage(pos: Int, page: Int): Boolean {
            return pos >= page*pageSize && pos < (page+1)*pageSize
        }
    }

    class SnapLayoutHolder(val snapLayout: SnapLayout) : RecyclerView.ViewHolder(snapLayout)
}
