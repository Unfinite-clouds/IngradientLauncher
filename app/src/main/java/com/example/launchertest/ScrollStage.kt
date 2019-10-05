package com.example.launchertest

import android.content.ClipData
import android.content.Context
import android.graphics.PointF
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

val FLIP_ZONE = toPx(40).toInt()

class ScrollStage(context: Context) : BaseStage(context), View.OnLongClickListener, View.OnDragListener {
    var apps = AppManager.mainScreenApps
//    val scrollId = R.id.main_stage_scroll
    override val stageLayoutId = R.layout.stage_0_main_screen
    lateinit var recyclerView: RecyclerViewScroll
    var widthCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_WIDTH_CELL, -1)
    var heightCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_HEIGHT_CELL, -1)

    override fun inflateAndAttach(rootLayout: ViewGroup) {
        super.inflateAndAttach(rootLayout)
        recyclerView = rootLayout.findViewById(R.id.stage_0_recycler)
        recyclerView.adapter = RecyclerListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rootLayout.setOnDragListener(this)
    }

    inner class RecyclerListAdapter : RecyclerView.Adapter<AppShortcutHolder>() {
        override fun getItemCount() = apps.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppShortcutHolder {
            val holder = AppShortcutHolder(LayoutInflater.from(context).inflate(R.layout.stage_0_vh, parent, false))
            holder.cell.apply {
                setOnDragListener(this@ScrollStage)
                layoutParams = LinearLayout.LayoutParams(widthCell,heightCell)
            }
            holder.cell.shortcut?.apply {
                setOnLongClickListener(this@ScrollStage)
                setPadding(0)
            }
            return holder
        }

        override fun onBindViewHolder(holder: AppShortcutHolder, position: Int) {
            holder.cell.shortcut?.appInfo = AppManager.getApp(apps[position])!!
            holder.cell.shortcut?.translationX = 0f
        }
    }

    class AppShortcutHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cell = itemView as DummyCell
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
        startPos = getAppPosition(v)
        destPos = -1
        dragShortcut = v
        isEnded = false
        hasDrop = false
        isFirstDrag = true
        v.startDrag(ClipData.newPlainText("",""), v.createDragShadow(), null, 0)
    }

    private var startPos: Int = -1
    private var destPos: Int = -1
    private var dragShortcut: AppShortcut? = null
    private var isEnded = false
    private var hasDrop = false
    private var isFirstDrag = true

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        if (v == null)
            return false

        when (event?.action) {

            DragEvent.ACTION_DRAG_STARTED -> {}

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (v is DummyCell) {
                    translate(startPos, destPos, 0f)
                    destPos = getAppPosition(v.shortcut!!)
                    translate(startPos, destPos, 100f)
                } else if (v is FrameLayout) {
                    translate(startPos, destPos, 0f)
                }
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                if (isFirstDrag) isFirstDrag = false else dragShortcut?.dismissMenu()

                if (v is DummyCell) {
                    val parentPoint = toParentCoords(v, event)
                    recyclerView.checkAndScroll(parentPoint.x, parentPoint.y)
                }
/*                when {
                    v !is DummyCell -> when {
                        // v is root - FrameLayout
                        event.x > v.width - SCROLL_ZONE -> startScroll(10)
                        event.x < SCROLL_ZONE -> startScroll(-10)
                        event.y > v.height - FLIP_ZONE -> { println("DOWN"); stopScroll() }
                        else -> stopScroll()
                    }
                    v is RecyclerView -> v
                }*/
//                cell.parentGrid.tryFlipPage(cell, event)
            }

            DragEvent.ACTION_DRAG_EXITED -> {}

            DragEvent.ACTION_DROP -> {
                // cell is the cell to drop
                if (v is DummyCell) {
                    resolvePositions(startPos, destPos)
                    hasDrop = true
                } else if (v is FrameLayout) {
                    removeApp(startPos)
                }

            }

            DragEvent.ACTION_DRAG_ENDED -> {
                if (!hasDrop) {
                    // drag has been canceled
                    translate(startPos, destPos, 0f)
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

    private fun toParentCoords(v: View, event: DragEvent): PointF {
        return PointF(v.left + event.x, v.top + event.y)
    }

    private fun translate(startPos: Int, destPos: Int, value: Float) {
        if (startPos == destPos || destPos == -1)
            return
        val direction = if (startPos < destPos) 1 else -1
        var pos = startPos
        while (pos != destPos) {
            pos+=direction
            findAppAtPosition(pos)?.translationX = value*direction*-1
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



    private fun removeApp(startPos: Int) {
        apps.removeAt(startPos)
    }

    private fun saveData() {
        AppManager.applyMainScreenChanges(context, apps)
    }

    private fun updateView() {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun findAppAtPosition(position: Int): AppShortcut? {
        return (recyclerView.findViewHolderForAdapterPosition(position) as? AppShortcutHolder)?.cell?.shortcut
    }

    private fun getAppPosition(v: AppShortcut): Int {
        return recyclerView.getChildAdapterPosition(v.parent as View)
    }
}