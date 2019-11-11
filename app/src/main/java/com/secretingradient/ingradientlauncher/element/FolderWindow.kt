package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.data.Data
import com.secretingradient.ingradientlauncher.data.Dataset
import com.secretingradient.ingradientlauncher.data.FolderInfo
import com.secretingradient.ingradientlauncher.data.Info
import com.secretingradient.ingradientlauncher.moveStack
import com.secretingradient.ingradientlauncher.stage.AppHolder
import kotlin.math.ceil
import kotlin.math.sqrt

class FolderWindow(context: Context, val dataset: Dataset<Data, Info>, var appSize: Int) : RecyclerView(context) {
    lateinit var folderView: FolderView
    var folderPosInDataset = -1
    private var selectedAppHolder: AppHolder? = null

    init {
        adapter = FolderAdapter()
        layoutParams = LayoutParams(0, 0)
//        setHasFixedSize(true)
        clipChildren = false
        clipToPadding = false
        ItemTouchHelper(ItemDragger()).attachToRecyclerView(this)
        setBackgroundColor(Color.GRAY)
    }

    fun setContent(folderView: FolderView) {
        // currently set Grid sizes 2 x 2 and 3 x 3
        val size = folderView.apps.size
        var columnsCount = ceil(sqrt(size.toFloat())).toInt()
        if (columnsCount == 1) columnsCount = 2
        else if (columnsCount == 2 && size == 4) columnsCount = 3

        layoutManager = GridLayoutManager(context, columnsCount)
        layoutParams.width = appSize * columnsCount
        layoutParams.height = appSize * columnsCount
        this.folderView = folderView
        folderPosInDataset = (folderView.layoutParams as SnapLayout.SnapLayoutParams).position
        adapter!!.notifyDataSetChanged()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP && selectedAppHolder != null) {
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (x < left || x > right || y < top || y > bottom) {
                val pos = selectedAppHolder!!.adapterPosition
                folderView.apps.removeAt(pos)
                dataset.put(folderPosInDataset, folderView.info, true)
                adapter!!.notifyItemRemoved(pos)
                return true
            }
        }
        return super.onTouchEvent(ev)
    }


    inner class FolderAdapter : RecyclerView.Adapter<AppHolder>() {
        override fun getItemCount() = dataset.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
            return AppHolder(AppView(context).apply {
                layoutParams = LayoutParams(appSize, appSize)
            })
        }

        override fun onBindViewHolder(holder: AppHolder, position: Int) {
            holder.app.info = (dataset[folderPosInDataset] as FolderInfo).apps[position]
        }
    }


    inner class ItemDragger : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition
            folderView.apps.moveStack(from, to)
            recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            selectedAppHolder = viewHolder as AppHolder
            super.onSelectedChanged(viewHolder, actionState)
        }
    }
}