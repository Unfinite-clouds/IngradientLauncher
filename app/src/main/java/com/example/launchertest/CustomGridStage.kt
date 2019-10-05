package com.example.launchertest

import android.content.ClipData
import android.content.Context
import android.graphics.Point
import android.view.DragEvent
import android.view.View
import androidx.core.view.forEach
import androidx.core.view.setPadding

class CustomGridStage(context: Context) : BasePagerStage(context), View.OnDragListener, View.OnLongClickListener {
    var apps = AppManager.customGridApps
    var rowCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_ROW_COUNT, -1)
    var columnCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_COLUMN_COUNT, -1)
    var pageCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_PAGE_COUNT, -1)
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_1_custom_grid
    override val viewPagerId = R.id.custom_grid_vp
    override val stageAdapter = CustomGridAdapter(context) as StageAdapter

    inner class CustomGridAdapter(context: Context) : BasePagerStage.StageAdapter(context) {
        override fun getItemCount() = pageCount

        override fun createPage(context: Context, page: Int): LauncherPageGrid {
            val grid = LauncherPageGrid(context, rowCount, columnCount, page, this@CustomGridStage)

            grid.forEach {
                it.setOnDragListener(this@CustomGridStage)
            }

            var appInfo: AppInfo?
            apps.forEach {
                if (it.key in grid.gridBounds) {
                    appInfo = AppManager.getApp(it.value)
                    if (appInfo != null)
                        grid.addShortcut(createAppShortcut(appInfo!!), it.key)
                }
            }
            return grid
        }
    }

    fun createAppShortcut(appInfo: AppInfo): AppShortcut {
        return AppShortcut(context, appInfo).apply {
            setOnLongClickListener(this@CustomGridStage)
            setPadding(cellPadding)
        }
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppShortcut) {
            v.showMenu()
            v.startDrag(ClipData.newPlainText("",""), v.createDragShadow(), Pair(v.parent as DummyCell, v), 0)
        }
        return true
    }

    private var dragSide = Point()
    private var dragCell: DummyCell? = null
    private var dragShortcut: AppShortcut? = null
    private var isEnded = false
    private var hasDrop = false
    private var isFirstDrag = true

    override fun onDrag(cell: View?, event: DragEvent): Boolean {
        // cell is the cell under finger
        if (cell !is DummyCell) return false

        when (event.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                if (dragShortcut == null) {
                    // will be called only once per drag event
                    val state = (event.localState as Pair<DummyCell?, AppShortcut>)
                    dragCell = state.first
                    if (dragCell == null){
                        dragShortcut = createAppShortcut(state.second.appInfo)
                    } else {
                        dragCell?.removeAllViews()
                        dragShortcut = state.second
                    }
                    isEnded = false
                    hasDrop = false
                }
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                cell.setBackgroundResource(R.drawable.bot_gradient)
                dragSide = Point(0, 0)
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                // remember the origin of coordinate system is [left, top]
                val newDragSide: Point =
                    if (event.y > event.x)
                        if (event.y > cell.height - event.x) Point(0, 1) else Point(-1, 0)
                    else
                        if (event.y > cell.height - event.x) Point(1, 0) else Point(0, -1)

                if (dragSide != newDragSide) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                    dragSide = newDragSide
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 100f)
                }

                if (isFirstDrag) isFirstDrag = false else dragShortcut?.dismissMenu()

                cell.parentGrid.tryFlipPage(cell, event)
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                cell.defaultState()
            }

            DragEvent.ACTION_DROP -> {
                // cell is the cell to drop
//                    dragShortcut?.icon?.clearColorFilter()
                if (cell.canMoveBy(-dragSide.x, -dragSide.y)) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating - just for prevent blinking
                    cell.doMoveBy(-dragSide.x, -dragSide.y)
                    cell.shortcut = dragShortcut
                    hasDrop = true
                } else {
                    return false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                if (!hasDrop) { // TODO: maybe move down?
                    // drag has been canceled
                    if (dragShortcut?.goingToRemove == false) {
                        dragCell?.shortcut = dragShortcut
                    } else {
                        // do nothing to let this shortcut to stay null and then deleted
                    }
                }
                if (!isEnded) {
                    // will be called only once per drag event
                    isEnded = true
                    cell.parentGrid.dragEnded()
                    cell.parentGrid.saveState()
//                        dragShortcut?.icon?.clearColorFilter()
                    dragCell = null
                    dragShortcut = null
                }
                cell.defaultState()
            }
        }
        return true
    }

}