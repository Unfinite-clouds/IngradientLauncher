package com.example.launchertest

import android.content.ClipData
import android.content.Context
import android.graphics.PointF
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScrollStage(context: Context) : BaseStage(context), View.OnLongClickListener, View.OnDragListener {
    var apps = AppManager.mainScreenApps
//    val scrollId = R.id.main_stage_scroll
    override val stageLayoutId = R.layout.stage_0_main_screen
    lateinit var container: RecyclerView
    var widthCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_WIDTH_CELL, -1)
    var heightCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_HEIGHT_CELL, -1)

    override fun inflateAndAttach(rootLayout: ViewGroup) {
        super.inflateAndAttach(rootLayout)
        container = rootLayout.findViewById(R.id.main_stage_app_container)
        container.adapter = RecyclerListAdapter()
        container.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        (container.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.BOTTOM
        rootLayout.setOnDragListener(this)
    }

    inner class RecyclerListAdapter : RecyclerView.Adapter<AppShortcutHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppShortcutHolder {
            val holder = AppShortcutHolder(LayoutInflater.from(context).inflate(R.layout.stage_0_vh, parent, false))
            holder.itemView.apply {
                setOnDragListener(this@ScrollStage)
                layoutParams = LinearLayout.LayoutParams(widthCell,heightCell)
            }
            holder.app.apply {
                setOnLongClickListener(this@ScrollStage)
                setPadding(0)
            }
            return holder
        }

        override fun getItemCount() = apps.size

        override fun onBindViewHolder(holder: AppShortcutHolder, position: Int) {
            holder.app.appInfo = AppManager.getApp(apps[position])!!
            holder.app.translationX = 0f
        }

    }

    class AppShortcutHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val app = (itemView as ViewGroup).getChildAt(0) as AppShortcut
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppShortcut) {
            v.showMenu()
            startDrag(v)
        }
        return true
    }

    private fun startDrag(v: AppShortcut) {
        // will be called only once per drag event
        v.visibility = View.INVISIBLE
        startPos = getPosition(v)
        destPos = -1
        dragShortcut = v
        isEnded = false
        hasDrop = false
        touchStartPoint = null
        isFirstDrag = true
        v.startDrag(ClipData.newPlainText("",""), v.createDragShadow(), null, 0)
    }

    private var touchStartPoint: PointF? = null
    private var startPos: Int = -1
    private var destPos: Int = -1
    private var dragShortcut: AppShortcut? = null
    private var isEnded = false
    private var hasDrop = false
    private var isFirstDrag = true

    override fun onDrag(v: View?, event: DragEvent?): Boolean {

        when (event?.action) {

            DragEvent.ACTION_DRAG_STARTED -> {}

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (v is FrameLayout) {
                    translate(startPos, destPos, 0f)
                    destPos = getPosition(v.getChildAt(0) as AppShortcut)
                    translate(startPos, destPos, 100f)
                } else if (v is LinearLayout) {
                    translate(startPos, destPos, 0f)
                }
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                if (isFirstDrag) isFirstDrag = false else dragShortcut?.dismissMenu()

//                cell.parentGrid.tryFlipPage(cell, event)
            }

            DragEvent.ACTION_DRAG_EXITED -> {
/*                if (v is AppShortcut)
                    translate(startPos, destPos, 0f)*/

//                back translating is needed only when view is being out of container
            }

            DragEvent.ACTION_DROP -> {
                // cell is the cell to drop
                if (v is FrameLayout) {
                    resolvePositions(startPos, destPos)
                    hasDrop = true
                }

            }

            DragEvent.ACTION_DRAG_ENDED -> {
                if (!hasDrop) {
                    // drag has been canceled

                }
                if (!isEnded) {
                    // will be called only once per drag event
                    dragShortcut?.visibility = View.VISIBLE
                    isEnded = true
                    saveData()
                    updateView()
                }
            }
        }
        return true
    }

    private fun translate(startPos: Int, destPos: Int, value: Float) {
        if (startPos == destPos || destPos == -1)
            return
        val direction = if (startPos < destPos) 1 else -1
        var pos = startPos
        while (pos != destPos) {
            pos+=direction
            getViewAtPosition(pos).translationX = value*direction*-1
        }
    }

    private fun resolvePositions(startPos: Int, destPos: Int) {
        if (startPos == destPos || destPos == -1)
            return
        val direction = if (startPos < destPos) 1 else -1
        val temp = apps[startPos]
        var pos = startPos
        while (pos != destPos) {
            apps[pos] = apps[pos + direction]
            pos+=direction
        }
        apps[destPos] = temp
    }

    private fun saveData() {
        AppManager.applyMainScreenChanges(context, apps)
    }

    private fun updateView() {
        container.adapter?.notifyDataSetChanged()
    }

    private fun getViewAtPosition(position: Int): AppShortcut {
        println(position)
        return (container.layoutManager?.findViewByPosition(position) as ViewGroup?)?.getChildAt(0) as AppShortcut
    }

    private fun getPosition(v: AppShortcut): Int {
        return container.getChildAdapterPosition(v.parent as View)
    }
}