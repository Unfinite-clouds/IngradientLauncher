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
import com.secretingradient.ingradientlauncher.AppManager
import com.secretingradient.ingradientlauncher.Preferences
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.getPrefs
import java.util.*

class MainStageRecycler : RecyclerView {
    var widthCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_WIDTH_CELL, -1)
    var heightCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_HEIGHT_CELL, -1)
    lateinit var apps: MutableList<String>
    private var itemTouchHelper: ItemTouchHelper
    var saveListener: OnSaveDataListener? = null
    var selectedAppHolder: ViewHolder? = null

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
            return AppHolder(AppView(context).apply {
                layoutParams = LinearLayout.LayoutParams(widthCell, heightCell)
            })
        }

        override fun onBindViewHolder(holder: AppHolder, position: Int) {
            holder.app.appInfo = AppManager.getApp(apps[position])!!
            holder.app.setOnClickListener(holder.app)
        }

        override fun onViewAttachedToWindow(holder: AppHolder) {
            holder.app.translationX = 0f
            holder.app.translationY = 0f
            holder.app.animatorScale.start()
            super.onViewAttachedToWindow(holder)
        }
    }

    class AppHolder(itemView: View) : ViewHolder(itemView) {
        val app = itemView as AppView
    }

    inner class TouchHelper : ItemTouchHelper.Callback() {
        private val duration = 500L
        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }

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
                this@MainStageRecycler.clipChildren = false
                (this@MainStageRecycler.parent as ViewGroup).clipChildren = false
            } else {
                saveListener?.onSaveData()
            }
            selectedAppHolder = viewHolder

            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
            // we need to wait until the RecoverAnimation ends
            recyclerView.postDelayed(duration) {
                recyclerView.clipChildren = true
                (recyclerView.parent as ViewGroup).clipChildren = true
            }
        }

        override fun getAnimationDuration(recyclerView: RecyclerView, animationType: Int, animateDx: Float, animateDy: Float): Long {
            return duration
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val r = Rect(0, 0, width, height)
        if (ev.action == MotionEvent.ACTION_UP && selectedAppHolder != null) {
            if (!r.contains(ev.x.toInt(), ev.y.toInt())) {
                adapter?.notifyItemRemoved(selectedAppHolder!!.adapterPosition)
                // TODO update apps
                return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    interface OnSaveDataListener {
        fun onSaveData()
    }
}