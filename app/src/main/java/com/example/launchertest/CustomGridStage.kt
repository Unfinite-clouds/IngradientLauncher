package com.example.launchertest

import android.content.ClipData
import android.content.Context
import android.graphics.Point
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.setPadding

class CustomGridStage(context: Context) : BasePagerStage(context), View.OnDragListener, View.OnLongClickListener {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = AppManager.customGridApps
    var rowCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_ROW_COUNT, -1)
    var columnCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_COLUMN_COUNT, -1)
    var pageCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_PAGE_COUNT, -1)
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_1_custom_grid
    override val viewPagerId = R.id.custom_grid_vp
    override val stageAdapter = CustomGridAdapter(context) as StageAdapter

    override fun inflateAndAttach(rootLayout: ViewGroup) {
        super.inflateAndAttach(rootLayout)
        val trash = rootLayout.findViewById<RemoveZoneView>(R.id.trash_zone)
    }

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
        return AppShortcut(context, appInfo).apply { adaptApp(this) }
    }

    override fun adaptApp(app: AppShortcut) {
        app.setOnLongClickListener(this@CustomGridStage)
        app.setPadding(cellPadding)
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppShortcut) {
            v.showMenu()
            startDrag(v)
        }
        return true
    }

    private var direction = Point()
    private var dragCell: DummyCell? = null
    private var dragShortcut: AppShortcut? = null
    private var isFirstDrag = true

    override fun startDrag(v: View) {
        if (v is AppShortcut) {
            dragShortcut = v
            dragCell = v.parent as DummyCell
            v.startDrag(ClipData.newPlainText("",""), v.createDragShadow(), Pair(dragCell, dragShortcut), 0)
        }
    }

    override fun onFocus(event: DragEvent) {
        // it's time to handle this drag event
        isFirstDrag = true
        direction = Point(0, 0)

        if (dragShortcut != null && dragCell != null) {
            // we have started the drag event
            dragCell!!.shortcut = null
        } else {
            // drag becomes from other stage
            val state = event.localState as Pair<*, *>
            dragShortcut = state.second as AppShortcut
            adaptApp(dragShortcut!!) // we don't create a copy
        }

    }

    override fun onFocusLost(event: DragEvent) {
        dragShortcut?.visibility = View.VISIBLE
        //save state
    }

    override fun onDragEnded() {
        if (dragShortcut?.parent == null && dragCell != null) {
            //drag canceled
            dragCell?.shortcut = dragShortcut
        }
/*        cell.parentGrid.dragEnded()
        cell.parentGrid.saveState()
//      dragShortcut?.icon?.clearColorFilter()
        dragCell = null
        dragShortcut = null*/
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        super.onDrag(v, event)

        when (event?.action) {

            DragEvent.ACTION_DRAG_STARTED -> {}

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (v is DummyCell)
                    v.setBackgroundResource(R.drawable.bot_gradient)
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                if (isFirstDrag) isFirstDrag = false else dragShortcut?.dismissMenu()

                if (v is DummyCell) {
                    val newDirection: Point =
                        if (event.y > event.x)
                            if (event.y > v.height - event.x) Point(0, -1) else Point(1, 0)
                        else
                            if (event.y > v.height - event.x) Point(-1, 0) else Point(0, 1)

                    if (direction != newDirection) {
                        // TODO: doTranslateBy is shit
                        doTranslateBy(v, direction, 0f) // back translating
                        direction = newDirection
                        doTranslateBy(v, direction, 100f)
                    }

                    (v.parent as LauncherPageGrid).tryFlipPage(v, event)

                    if (event.y > v.top + FLIP_ZONE) {
                        flipToStage(0, event)
                    }
                }
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                if (v is DummyCell) {
                    doTranslateBy(v, direction, 0f) // back translating
                    v.defaultState()
                }
            }

            DragEvent.ACTION_DROP -> {
                // v is the view to drop
                if (v is RemoveZoneView) {
                    dragShortcut = null
                }
                else if (v is DummyCell && canMoveBy(v, direction)) {
                    doTranslateBy(v, direction, 0f) // back translating - just for prevent blinking
                    doMoveBy(v, direction)
                    v.shortcut = dragShortcut
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                if (v is DummyCell && !isEnded) {
                    // will be called only once per drag event
                    isEnded = true
                    // TODO: parentGrid, dragEnded, saveState are shits
                    val grid = v.parent as LauncherPageGrid
                    grid.dragEnded()
                    grid.saveState()
                    dragCell = null
                    dragShortcut = null

//                    endDrag()
                }
                if (v is DummyCell)
                    v.defaultState()
            }
        }
        return true
    }

    
    fun moveApp(from: DummyCell, to: DummyCell) {
        if (!to.isEmptyCell())
            throw LauncherException("trying to move app into occupied cell")
        else if (from.isEmptyCell())
            throw LauncherException("trying to move app from empty cell")

        val shortcutTemp = from.shortcut
        if (shortcutTemp != null) {
            from.removeAllViews()
            to.shortcut = shortcutTemp
            AppManager.applyCustomGridChanges(context, to.position, shortcutTemp.appInfo.id)
        }
    }

    private fun doRecursionPass(cell: DummyCell, direction: Point, action: (thisCell: DummyCell, nextCell: DummyCell) -> Unit): Boolean {
        if (cell.isEmptyCell()) {
            return true
        }
        if (direction.x == 0 && direction.y == 0) {
            action(cell, cell)
            return true
        }
        val next = Point(cell.relativePosition.x + direction.x, cell.relativePosition.y + direction.y)
        val nextCell: DummyCell? = (cell.parent as LauncherPageGrid).getCellAt(next)
        if (nextCell != null && doRecursionPass(nextCell, direction, action)) {
            action(cell, nextCell)
            return true
        }
        return false
    }

    fun canMoveBy(cell: DummyCell, direction: Point): Boolean {
        return doRecursionPass(cell, direction) { thisCell, nextCell -> }
    }

    fun doMoveBy(cell: DummyCell,direction: Point): Boolean {
        return doRecursionPass(cell, direction) { thisCell, nextCell ->
            moveApp(thisCell, nextCell)
        }
    }

    fun doTranslateBy(cell: DummyCell,direction: Point, value: Float): Boolean {
        return doRecursionPass(cell, direction) { thisCell, nextCell ->
            thisCell.shortcut?.translationX = value*direction.x
            thisCell.shortcut?.translationY = value*direction.y
        }
    }
}