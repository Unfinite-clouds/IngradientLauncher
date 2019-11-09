package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.element.AppInfo
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.vibrate

class FolderLayout : RecyclerView {
    lateinit var apps: MutableList<AppInfo>
//    lateinit var dataKeeper: DataKeeper
    var appSize = -1
    var columnsCount = 2
        set(value) {
            field = value
            (layoutManager as GridLayoutManager).spanCount = columnsCount
        }
    private var selectedAppHolder: ViewHolder? = null

    init {
        adapter = FolderAdapter()
        layoutManager = GridLayoutManager(context, columnsCount)
        ItemTouchHelper(ItemDragger()).attachToRecyclerView(this)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP && selectedAppHolder != null) {
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (x < left || x > right || y < top || y > bottom) {
                val pos = selectedAppHolder!!.adapterPosition
                apps.removeAt(pos)
                adapter?.notifyItemRemoved(pos)
                return true
            }
        }
        return super.onTouchEvent(ev)
    }


    inner class FolderAdapter : RecyclerView.Adapter<AppHolder>() {
        override fun getItemCount() = apps.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
            return AppHolder(AppView(context).apply {
                layoutParams = LayoutParams(appSize, appSize)
            })
        }

        override fun onBindViewHolder(holder: AppHolder, position: Int) {
            holder.appView.appInfo = apps[position]
        }
    }


    inner class ItemDragger : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
            recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            selectedAppHolder = viewHolder
            if (viewHolder != null) {
                vibrate(viewHolder.itemView.context)
                clipChildren = false
                clipToPadding = false
            } else {
                clipChildren = true
                clipToPadding = true
            }
            super.onSelectedChanged(viewHolder, actionState)
        }
    }
}