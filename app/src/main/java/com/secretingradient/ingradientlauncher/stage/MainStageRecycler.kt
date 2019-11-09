package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.Preferences
import com.secretingradient.ingradientlauncher.data.Dataset
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.getPrefs
import com.secretingradient.ingradientlauncher.vibrate

class MainStageRecycler : RecyclerView {
    var widthCell = getPrefs(context).getInt(Preferences.MAIN_STAGE_WIDTH_CELL, -1)
    var heightCell = getPrefs(context).getInt(Preferences.MAIN_STAGE_HEIGHT_CELL, -1)
    lateinit var dataset: Dataset<MainStage.AppData, MainStage.AppState>
    var itemTouchHelper: ItemTouchHelper
    var saveListener: OnSaveDataListener? = null
    var selectedAppHolder: ViewHolder? = null
    private val bounds = Rect()


    init {
        setHasFixedSize(true)
        adapter = RecyclerListAdapter()
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        itemTouchHelper = ItemTouchHelper(TouchHelper())
        itemTouchHelper.attachToRecyclerView(this)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun init(dataset: Dataset<MainStage.AppData, MainStage.AppState>) {
        this.dataset = dataset
    }

    inner class RecyclerListAdapter : RecyclerView.Adapter<AppHolder>() {
        override fun getItemCount() = dataset.dataset.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
            return AppHolder(createAppView())
        }

        override fun onBindViewHolder(holder: AppHolder, position: Int) {
            holder.appView.state = dataset.dataset[position]
            holder.appView.setOnClickListener(AppView)
        }

        override fun onViewAttachedToWindow(holder: AppHolder) {
            holder.appView.translationX = 0f
            holder.appView.translationY = 0f
            holder.appView.animatorScale.start()
            super.onViewAttachedToWindow(holder)
        }
    }

    fun createAppView(appId: String? = null): AppView {
//        var appInfo: AppInfo? = null
//        if (appId != null)
//            appInfo = DataKeeper.getAppInfoById(appId)
        return AppView(context/*, appInfo*/).apply {
            layoutParams = LinearLayout.LayoutParams(widthCell, heightCell)
        }
    }

    fun insertViewUnder(appView: AppView, x: Float, y: Float): Int {
        val targetPosition = layoutManager?.getPosition(findChildViewUnder(x, y)!!)!!
        dataset.insertItem(targetPosition, appView.state)
        adapter?.notifyItemInserted(targetPosition)
        return targetPosition
    }

    inner class TouchHelper : ItemTouchHelper.Callback() {
        private val duration = 300L
        override fun isItemViewSwipeEnabled() = false

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
            return makeMovementFlags(
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            )
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition
            dataKeeper.mainStageData.onMoved(from, to)
            recyclerView.adapter?.notifyItemMoved(from, to)
            return true
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            if (viewHolder != null) {
                vibrate(context)
                this@MainStageRecycler.clipChildren = false
                (this@MainStageRecycler.parent as ViewGroup).clipChildren = false
            } else {
                saveListener?.onSaveData()
            }
            selectedAppHolder = viewHolder
            println("selected: ${selectedAppHolder?.itemView}")

            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
            // wait until the RecoverAnimation ends
            recyclerView.postDelayed(duration) {
                recyclerView.clipChildren = true
                (recyclerView.parent as ViewGroup).clipChildren = true
            }
        }

        override fun getAnimationDuration(recyclerView: RecyclerView, animationType: Int, animateDx: Float, animateDy: Float) = duration

//        override fun getMoveThreshold(viewHolder: ViewHolder) = 0.6f

/*        override fun chooseDropTarget(selected: ViewHolder, dropTargets: MutableList<ViewHolder>, curX: Int, curY: Int): ViewHolder? {
            if (dropTargets.size > 1)
                return super.chooseDropTarget(selected, dropTargets, curX, curY)
            return dropTargets[0]
        }*/
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bounds.set(0,0,w,h)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP && selectedAppHolder != null) {
            if (!bounds.contains(x.toInt(), y.toInt())) {
                val pos = selectedAppHolder!!.adapterPosition
                dataKeeper.mainStageData.onRemoved(pos)
                adapter?.notifyItemRemoved(pos)
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    interface OnSaveDataListener {
        fun onSaveData()
    }
}