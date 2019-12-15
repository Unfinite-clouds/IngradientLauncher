package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.data.Data
import com.secretingradient.ingradientlauncher.data.Dataset
import com.secretingradient.ingradientlauncher.data.FolderInfo
import com.secretingradient.ingradientlauncher.data.Info
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.FolderView
import com.secretingradient.ingradientlauncher.moveStack
import kotlin.math.ceil
import kotlin.math.sqrt

class FolderWindow : RecyclerView {
    lateinit var dataset: Dataset<Data, Info>
    lateinit var folderInfo: FolderInfo
    lateinit var folderView: FolderView
    var inEditMode = false
    var appSize = -1
    var folderPosInDataset = -1
    private var selectedAppHolder: AppHolder? = null
    val selectedApp: AppView?
        get() = selectedAppHolder?.app
    val itemDragger: ItemTouchHelper

    init {
        adapter = FolderAdapter()
        setHasFixedSize(true)
        clipChildren = false
        clipToPadding = false
        itemDragger = ItemTouchHelper(ItemDragger()).apply { attachToRecyclerView(this@FolderWindow) }
        setBackgroundColor(Color.BLACK)
    }

    constructor(context: Context, dataset: Dataset<Data, Info>, appSize: Int) : super(context) {
        initData(dataset, appSize)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun initData(dataset: Dataset<Data, Info>, appSize: Int) {
        this.dataset = dataset
        this.appSize = appSize
    }

    fun setContent(folderView: FolderView, folderPosInDataset: Int) {
        this.folderView = folderView
        this.folderInfo = folderView.info
        this.folderPosInDataset = folderPosInDataset

        // currently set Grid sizes 2 x 2 and 3 x 3
        val size = folderInfo.apps.size
        var columnsCount = ceil(sqrt(size.toFloat())).toInt()
        if (columnsCount <= 1) columnsCount = 2
        else if (columnsCount == 2 && size == 4) columnsCount = 3

        layoutManager = GridLayoutManager(context, columnsCount)
        adapter!!.notifyDataSetChanged()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // positioning within parent's bounds
        super.onLayout(changed, l, t, r, b)
        val extraX = r - (parent as ViewGroup).width
        val extraY = b - (parent as ViewGroup).height
        if (extraX > 0) {
            left -= extraX
            right -= extraX
        }
        if (extraY > 0) {
            top -= extraY
            bottom -= extraY
        }
    }

    inner class FolderAdapter : RecyclerView.Adapter<AppHolder>() {
        override fun getItemCount() = folderInfo.apps.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
            return AppHolder(AppView(context).apply {
                layoutParams = LayoutParams(appSize, appSize)
            })
        }

        override fun onBindViewHolder(holder: AppHolder, position: Int) {
            holder.app.info = folderInfo.apps[position]
        }
    }

    fun removeSelectedApp() {
        if (selectedAppHolder != null) {
            val pos = selectedAppHolder!!.adapterPosition
            folderView.removeApp(pos)
            dataset.put(folderPosInDataset, folderInfo, true)
//            adapter?.notifyItemRemoved(pos)
//            if (folderInfo.apps.size < 2)
//                folderView.revert(dataset)
        }
    }

    fun startDrag(downEvent: MotionEvent) {
        println("startDrag in folder: ${downEvent.y}")
        val v = findChildViewUnder(downEvent.x, downEvent.y)
        if (v != null) {
            itemDragger.startDrag(getChildViewHolder(v))
            onInterceptTouchEvent(downEvent)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if ((parent as UserStage2).inEditMode && ev.action == MotionEvent.ACTION_DOWN) {
            startDrag(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    inner class ItemDragger : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
        override fun isLongPressDragEnabled(): Boolean = false

        override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition
            folderInfo.apps.moveStack(from, to)
            dataset.put(folderPosInDataset, folderInfo, true)
            recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            selectedAppHolder = viewHolder as? AppHolder
            super.onSelectedChanged(viewHolder, actionState)
        }
    }
}