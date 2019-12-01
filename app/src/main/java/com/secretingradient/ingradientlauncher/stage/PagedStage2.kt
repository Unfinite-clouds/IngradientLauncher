package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.LauncherException
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.SnapLayoutHoverable
import com.secretingradient.ingradientlauncher.data.Data
import com.secretingradient.ingradientlauncher.data.Dataset
import com.secretingradient.ingradientlauncher.data.Info

abstract class PagedStage2(context: Context, attrs: AttributeSet?) : BaseStage2(context, attrs) {
    abstract var columnCount: Int
    abstract var rowCount: Int
    abstract var pageCount: Int
    abstract val dataset: Dataset<Data, Info>

    val pageSize
        get() = rowCount*columnCount*4
    protected abstract val adapter: PagedAdapter
    abstract val stageVP: ViewPager2
    val stageRV: RecyclerView
        get() = stageVP.getChildAt(0) as RecyclerView
    val currentSnapLayout: SnapLayout
        get() = stageRV.getChildAt(0) as SnapLayout

    abstract fun bindPage(holder: PageHolder, page: Int)

    fun isPosInPage(pos: Int, page: Int): Boolean {
        return pos >= page*pageSize && pos < (page+1)*pageSize
    }

    fun placeFromDragLayer(draggedView: View, toPositionInCurrentPage: Int) {
        val fromParent = dragContext!!.dragController.realState.parent as? SnapLayout
        val toParent = currentSnapLayout
        val lp = draggedView.layoutParams as? SnapLayout.SnapLayoutParams
        check(fromParent is SnapLayout)
        check(toPositionInCurrentPage <= pageSize)
        check(lp is SnapLayout.SnapLayoutParams)

        if (toParent.canMoveViewToPos(draggedView, toPositionInCurrentPage)) {
            (draggedView.parent as ViewGroup).removeView(draggedView)
            val from = getPagedPosition(lp.position, fromParent)
            val to = getPagedPosition(toPositionInCurrentPage, currentSnapLayout)
            lp.position = toPositionInCurrentPage
            toParent.addView(draggedView)
            println("$from -> $to")
//            dragContext?.pendingActions dataset.move(from, to) todo
        }
    }

    fun getPagedPositionOfElement(element: View): Int {
        if (element.parent !is SnapLayout) throw LauncherException("element $element is not a child of SnapLayout")
        return getPagedPosition((element.layoutParams as SnapLayout.SnapLayoutParams).position, element.parent as SnapLayout)
    }

    fun getPagedPosition(pos: Int, snapLayout: SnapLayout): Int {
        return pos + getPageOfSnapLayout(snapLayout) * pageSize
    }

    fun getPageOfSnapLayout(snapLayout: SnapLayout): Int {
        return (stageRV.getChildViewHolder(snapLayout) as PageHolder).page
    }

    // lazy page creating is bad
    inner class PagedAdapter : RecyclerView.Adapter<PageHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
            return PageHolder(SnapLayoutHoverable(context, columnCount*2, rowCount*2 ).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }, -1)
        }

        override fun onBindViewHolder(holder: PageHolder, page: Int) {
            // TODO: shit method
            // we don't need to removeAllViews and add them again, just rebind existing AppViews instead?
            // or NO? how to handle widgets?
            // we can use SnapLayoutState and preload all elements in one array like 'pageStates', so we will use only once SnapLayout
            // actually we should use RecyclerView with custom SnapLayoutManager and SnapHelper instead of ViewPager2 (ye, I love hard ways :)
            // (I don't know how to do page transition animations with RecyclerView)

            holder.page = page
            holder.snapLayout.removeAllViews()

            bindPage(holder, page)
        }

        override fun getItemCount(): Int = pageCount
    }

    class PageHolder(val snapLayout: SnapLayout, var page: Int) : RecyclerView.ViewHolder(snapLayout)
}
