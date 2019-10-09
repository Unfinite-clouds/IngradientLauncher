package com.secretingradient.ingradientlauncher.stage

import android.content.ClipData
import android.content.Context
import android.graphics.Point
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.setPadding
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.element.AppInfo
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.DummyCell
import com.secretingradient.ingradientlauncher.element.TrashView

class UserStage(context: Context) : BasePagerStage(context), View.OnDragListener, View.OnLongClickListener {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = AppManager.customGridApps
    var rowCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_ROW_COUNT, -1)
    var columnCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_COLUMN_COUNT, -1)
    var pageCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_PAGE_COUNT, -1)
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_1_custom_grid
    override val viewPagerId = R.id.custom_grid_vp
    override val stageAdapter = CustomGridAdapter(context) as StageAdapter
    lateinit var trashView: TrashView

    override fun inflateAndAttach(rootLayout: ViewGroup) {
        super.inflateAndAttach(rootLayout)
        trashView = rootLayout.findViewById(R.id.trash_view)
        trashView.setOnDragListener(this)
    }

    inner class CustomGridAdapter(context: Context) : BasePagerStage.StageAdapter(context) {
        override fun getItemCount() = pageCount

        override fun createPage(context: Context, page: Int): SnapGridLayout {
            val grid = SnapGridLayout(context, rowCount, columnCount, page, this@UserStage)

            grid.forEach {
                it.setOnDragListener(this@UserStage)
            }

            var appInfo: AppInfo?
            apps.forEach {
                if (it.key in grid.gridBounds) {
                    appInfo = AppManager.getApp(it.value)
                    if (appInfo != null)
                        grid.putApp(createAppShortcut(appInfo!!), it.key)
                }
            }
            return grid
        }
    }

    fun createAppShortcut(appInfo: AppInfo): AppView {
        return AppView(context, appInfo).apply { adaptApp(this) }
    }

    override fun adaptApp(app: AppView) {
        app.setOnLongClickListener(this@UserStage)
        app.setPadding(cellPadding)
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppView) {
            v.showMenu()
            startDrag(v)
        }
        return true
    }

    private var direction = Point()
    private var dragCell: DummyCell? = null
    private var dragApp: AppView? = null
    private var isFirstDrag = true

    override fun startDrag(v: View) {
        if (v is AppView) {
            v.startDrag(ClipData.newPlainText("",""), v.createDragShadow(), DragState(v, this), 0)
        }
    }

    override fun onFocus(event: DragEvent) {
        // it's time to handle this drag event
        isFirstDrag = true
        direction = Point(0, 0)

        dragApp = if (isMyEvent(event)) getParcelApp(event) else createAppShortcut(getParcelApp(event).appInfo)
        dragCell = if (isMyEvent(event)) dragApp!!.parent as DummyCell else null

        if (isMyEvent(event)) {
            // we have started the drag event
            removeApp(dragCell!!)
        } else {
            // drag becomes from other stage
            adaptApp(dragApp!!) // we don't create a copy
        }
        trashView.activate()

    }

    override fun onFocusLost(event: DragEvent) {}

    override fun onDragEnded(event: DragEvent) {
        dragCell = null
        dragApp = null
        trashView.deactivate()
        saveState()
/*        cell.parentGrid.dragEnded()
        cell.parentGrid.saveState()
//      dragApp?.icon?.clearColorFilter()
        dragCell = null
        dragApp = null*/
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
                if (isFirstDrag) isFirstDrag = false else dragApp?.dismissMenu()

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

                    (v.parent as SnapGridLayout).tryFlipPage(v, event)

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
                if (v == trashView) {
                    dragApp = null
                }
                else if (v is DummyCell && canMoveBy(v, direction)) {
                    doTranslateBy(v, direction, 0f) // back translating - just for prevent blinking
                    doMoveBy(v, direction)
                    putApp(dragApp!!, v)
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                if (v is DummyCell && !isEnded) {
                    // will be called only once per drag event
                    isEnded = true
                    // TODO: parentGrid, dragEnded, saveState are shits
                    val grid = v.parent as SnapGridLayout
                    grid.dragEnded()

//                    endDrag()
                }
                if (v is DummyCell)
                    v.defaultState()
            }
        }
        return true
    }

    private fun saveState() {
        AppManager.applyCustomGridChanges(context, apps)
    }

    private fun putApp(app: AppView, to: DummyCell) {
        to.app = app
        apps[to.position] = app.appInfo.id
    }

    private fun removeApp(cell: DummyCell) {
        cell.app = null
        apps.remove(cell.position)
    }
    
    private fun moveApp(from: DummyCell, to: DummyCell) {
        if (!to.isEmptyCell())
            throw LauncherException("trying to move app into occupied cell")
        else if (from.isEmptyCell())
            throw LauncherException("trying to move app from empty cell")

        val shortcutTemp = from.app
        if (shortcutTemp != null) {
            from.removeAllViews()
            to.app = shortcutTemp

            apps[to.position] = apps[from.position]!!
            apps.remove(from.position)
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
        val nextCell: DummyCell? = (cell.parent as SnapGridLayout).getCellAt(next)
        if (nextCell != null && doRecursionPass(nextCell, direction, action)) {
            action(cell, nextCell)
            return true
        }
        return false
    }

    private fun canMoveBy(cell: DummyCell, direction: Point): Boolean {
        return doRecursionPass(cell, direction) { thisCell, nextCell -> }
    }

    private fun doMoveBy(cell: DummyCell, direction: Point): Boolean {
        return doRecursionPass(cell, direction) { thisCell, nextCell ->
            moveApp(thisCell, nextCell)
        }
    }

    private fun doTranslateBy(cell: DummyCell, direction: Point, value: Float): Boolean {
        return doRecursionPass(cell, direction) { thisCell, nextCell ->
            thisCell.app?.translationX = value*direction.x
            thisCell.app?.translationY = value*direction.y
        }
    }
}