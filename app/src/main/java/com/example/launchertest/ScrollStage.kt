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
import com.example.launchertest.RecyclerViewScroll.Companion.SCROLL_ZONE

class ScrollStage(context: Context) : BaseStage(context), View.OnLongClickListener, View.OnDragListener {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = AppManager.mainScreenApps
    override val stageLayoutId = R.layout.stage_0_main_screen
    lateinit var recyclerView: RecyclerViewScroll
    var widthCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_WIDTH_CELL, -1)
    var heightCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_HEIGHT_CELL, -1)

    override fun inflateAndAttach(rootLayout: ViewGroup) {
        super.inflateAndAttach(rootLayout)
        recyclerView = rootLayout.findViewById(R.id.stage_0_recycler)
        recyclerView.adapter = RecyclerListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.apps = apps
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
            adaptApp(holder.cell.app!!)
            return holder
        }

        override fun onBindViewHolder(holder: AppShortcutHolder, position: Int) {
            holder.cell.app?.appInfo = AppManager.getApp(apps[position])!!
            holder.cell.app?.translationX = 0f
        }
    }

    class AppShortcutHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cell = itemView as DummyCell
    }

    override fun adaptApp(app: AppView) {
        app.setOnLongClickListener(this@ScrollStage)
        app.setPadding(0)
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppView) {
            v.showMenu()
            startDrag(v)
        }
        return true
    }

    private var dragApp: AppView? = null
    private var isFirstDrag = true

    override fun startDrag(v: View) {
        if (v is AppView) {
            v.visibility = View.INVISIBLE
            v.startDrag(ClipData.newPlainText("", ""), v.createDragShadow(), DragState(v, this), 0)
        }
    }

    override fun onFocus(event: DragEvent) {
        // it's time to handle this drag event
        isFirstDrag = true
        updateView()
        dragApp = getParcelApp(event)

        if (isMyEvent(event)) {
            // we have started the drag event
            recyclerView.dragStarted(dragApp!!.parent as DummyCell)
        } else {
            // drag becomes from other stage
            recyclerView.dragStartedWithNew(dragApp!!.appInfo.id)
        }
    }

    override fun onFocusLost(event: DragEvent) {
        recyclerView.resetTranslate()
        recyclerView.stopDragScroll()
    }

    override fun onDragEnded(event: DragEvent) {
        println("end Scroll")
        if (isMyEvent(event)) {
            if (hasFocus)
                dragApp?.visibility = View.VISIBLE
            else
                removeApp()
        }
        recyclerView.resetTranslate()
        recyclerView.stopDragScroll()
        saveData()
        updateView()
        dragApp = null
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        super.onDrag(v, event)

        when (event?.action) {

            DragEvent.ACTION_DRAG_STARTED -> {}

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (v is DummyCell) {
                    recyclerView.handleTranslate(v)
                } else if (v is FrameLayout) {
                    recyclerView.resetTranslate()
                }
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                if (isFirstDrag) isFirstDrag = false else dragApp?.dismissMenu()

                if (v is DummyCell) {
                    recyclerView.checkAndScroll(toParentCoords(v, event))
                } else if (v is FrameLayout) {
                    // v is root - FrameLayout
                    when {
                        event.x > v.width - SCROLL_ZONE -> recyclerView.startDragScroll(+1)
                        event.x < SCROLL_ZONE -> recyclerView.startDragScroll(-1)
                        event.y > v.height - FLIP_ZONE -> flipToStage(1, event)
                        else -> recyclerView.stopDragScroll()
                    }
                }
            }

            DragEvent.ACTION_DRAG_EXITED -> {}

            DragEvent.ACTION_DROP -> {
                if (v is DummyCell) {
                    resolvePositions()
                } else if (v is FrameLayout) {
                    if (recyclerView.startPos < apps.size)
                        removeApp()
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {}
        }
        return true
    }

    private fun toParentCoords(v: View, event: DragEvent): PointF {
        return PointF(v.left + event.x, v.top + event.y)
    }

    private fun resolvePositions() {
        val startPos = recyclerView.startPos
        val destPos = recyclerView.destPos

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

    private fun removeApp() {
        apps.removeAt(recyclerView.startPos)
    }

    private fun saveData() {
        AppManager.applyMainScreenChanges(context, apps)
    }

    private fun updateView() {
        recyclerView.adapter?.notifyDataSetChanged()
    }
}