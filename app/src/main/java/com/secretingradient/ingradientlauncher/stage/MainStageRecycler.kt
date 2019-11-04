package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.DataKeeper
import com.secretingradient.ingradientlauncher.Preferences
import com.secretingradient.ingradientlauncher.element.AppInfo
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.getPrefs
import com.secretingradient.ingradientlauncher.vibrate
import java.util.*

class MainStageRecycler : RecyclerView {
    var widthCell = getPrefs(context).getInt(Preferences.MAIN_STAGE_WIDTH_CELL, -1)
    var heightCell = getPrefs(context).getInt(Preferences.MAIN_STAGE_HEIGHT_CELL, -1)
    lateinit var apps: MutableList<String>
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

    inner class RecyclerListAdapter : RecyclerView.Adapter<AppHolder>() {
        override fun getItemCount() = apps.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
            return AppHolder(createAppView())
        }

        override fun onBindViewHolder(holder: AppHolder, position: Int) {
            holder.app.appInfo = DataKeeper.getAppInfoById(apps[position])!!
            holder.app.setOnClickListener(AppView)
        }

        override fun onViewAttachedToWindow(holder: AppHolder) {
            holder.app.translationX = 0f
            holder.app.translationY = 0f
            holder.app.animatorScale.start()
            super.onViewAttachedToWindow(holder)
        }
    }

    fun createAppView(appId: String? = null): AppView {
        var appInfo: AppInfo? = null
        if (appId != null)
            appInfo = DataKeeper.getAppInfoById(appId)
        return AppView(context, appInfo).apply {
            layoutParams = LinearLayout.LayoutParams(widthCell, heightCell)
        }
    }

    class AppHolder(itemView: View) : ViewHolder(itemView) {
        val app = itemView as AppView
    }

    fun insertViewUnder(appView: AppView, x: Float, y: Float): Int {
        val targetPosition = layoutManager?.getPosition(findChildViewUnder(x, y)!!)!!
        apps.add(targetPosition, appView.appInfo.id)
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
            Collections.swap(apps, viewHolder.adapterPosition, target.adapterPosition)
            recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP && selectedAppHolder != null) {
            if (!hitPoint(ev.x, ev.y)) {
                val pos = selectedAppHolder!!.adapterPosition
                apps.removeAt(pos)
                adapter?.notifyItemRemoved(pos)
                return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun <T: Number> hitPoint(x: T, y: T): Boolean {
        return bounds.contains(x.toInt(), y.toInt())
    }

    interface OnSaveDataListener {
        fun onSaveData()
    }
}