package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.*
import android.widget.ImageView
import androidx.core.view.children
import androidx.core.view.setPadding
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.element.AppInfo
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.TrashView

class UserStage(context: Context) : BasePagerSnapStage(context) {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = DataKeeper.userStageAppsData
    var folders = DataKeeper.userStageFoldersData
    override var columnCount = getPrefs(context).getInt(Preferences.USER_STAGE_COLUMN_COUNT, -1)
    override var rowCount = getPrefs(context).getInt(Preferences.USER_STAGE_ROW_COUNT, -1)
    override var pageCount = getPrefs(context).getInt(Preferences.USER_STAGE_PAGE_COUNT, -1)
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_1_custom_grid
    override val viewPagerId = R.id.user_stage_pager
    override val pagerAdapter = PagerSnapAdapter(apps, folders)
    lateinit var trashView: TrashView
    val currentSnapLayout: SnapLayout
        get() = (stageViewPager.getChildAt(0) as ViewGroup).getChildAt(0) as SnapLayout

    override fun inflateAndAttach(stageRoot: StageRoot) {
        super.inflateAndAttach(stageRoot)
        trashView = stageRoot.findViewById(R.id.trash_view)
        stageRoot.setOnTouchListener(this)
        trashView.setOnTouchListener(this)
//        stageViewPager.offscreenPageLimit = 2
        (stageViewPager.getChildAt(0) as ViewGroup).clipChildren = false
    }

    fun createAppView(appInfo: AppInfo): AppView {
        return AppView(context, appInfo).apply { adaptApp(this) }
    }

    override fun adaptApp(app: AppView) {
        app.setPadding(cellPadding)
    }

    inner class EditModeListener : GestureDetector.SimpleOnGestureListener() { // todo
        var selected: AppView? = null
        private val ghostView = ImageView(context).apply { setBackgroundColor(Color.LTGRAY); layoutParams = SnapLayout.SnapLayoutParams(-1,2,2) }
        private var inEditMode = false
        private val scaleInEditMode = 0.85f
        private val selectedPivot = Point()
        private val touchPoint = Point()
        private val localPoint = Point()
        private var newPos = -1

        fun onDown(v: View, event: MotionEvent) {
            selected = v as? AppView
            if (selected != null) {
                selectedPivot.set(selected!!.left + stageViewPager.left + event.x.toInt(), selected!!.top + stageViewPager.top + event.y.toInt())
                selected!!.animatorScale.start()
                stageRoot.shouldIntercept = true
            }
            if (inEditMode) {
                stageRoot.parent.requestDisallowInterceptTouchEvent(true)
            }
            if (inEditMode && selected == null) {
                endEditMode()
            }
        }

        fun onMove(v: View, event: MotionEvent) {
            touchPoint.set(event.x.toInt(), event.y.toInt())

            // TODO: test for perform fast swipe
            // if (isSwipe) ...
            if (selected == null || !inEditMode)
                return

            selected!!.translationX = (event.x - selectedPivot.x)/scaleInEditMode
            selected!!.translationY = (event.y - selectedPivot.y)/scaleInEditMode

            val hitedView = getHitView(touchPoint)

            when (hitedView) {
                is TrashView -> hitedView.activate()
                is ViewPager2 -> {
                    val snap = currentSnapLayout
                    localPoint.set(touchPoint.x - stageViewPager.left, touchPoint.y - stageViewPager.top)
                    newPos = snap.getPosSnapped(localPoint, 2)
                    if (snap.canPlaceViewToPos(ghostView, newPos, selected)) {
                        if (ghostView.parent == null)
                            snap.addNewView(ghostView, newPos, 2, 2)
                        else {
                            snap.moveView(ghostView, newPos)
                        }
                    }
                }
            }
            (hitedView as? TrashView)?.activate()
        }

        fun onUp(v: View, event: MotionEvent) {
            if (inEditMode && selected != null) {
                val snap = currentSnapLayout
                val oldPos = (selected!!.layoutParams as SnapLayout.SnapLayoutParams).position
                snap.removeView(ghostView)
                snap.moveView(selected!!, newPos)
                // save state
                apps.remove(oldPos)
                apps[newPos] = selected!!.appInfo.id
                DataKeeper.dumpUserStageApps(context)
            }
            unselect()
            stageRoot.shouldIntercept = false
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            startEditMode()
        }

        private fun startEditMode() {
            inEditMode = true
            stageRoot.parent.requestDisallowInterceptTouchEvent(true)
            stageViewPager.animate().scaleX(scaleInEditMode).scaleY(scaleInEditMode).start()
        }

        private fun endEditMode() {
            unselect()
            stageViewPager.animate().scaleX(1f).scaleY(1f).start()
            inEditMode = false
            stageRoot.shouldIntercept = false
        }

        private fun unselect() {
            selected?.let {
                it.translationX = 0f
                it.translationY = 0f
            }
            selected = null
        }
    }

    private val editModeListener = EditModeListener()
    private val editModeDetector = GestureDetector(context, editModeListener)

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // todo remove
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            println("${MotionEvent.actionToString(event.action)} x = ${event.x} y = ${event.y} selected = ${editModeListener.selected?.javaClass?.simpleName}, v = ${v.javaClass.simpleName}")
        }

        editModeDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                editModeListener.onDown(v, event)
            }

            MotionEvent.ACTION_MOVE -> {
                editModeListener.onMove(v, event)
            }

            MotionEvent.ACTION_UP -> {
                editModeListener.onUp(v, event)
            }
        }

        return true
    }

    private var lastHited: View? = null
    private val hitRect = Rect()

    private fun getHitView(p: Point): View {
        lastHited?.getHitRect(hitRect)
        if (lastHited != null && hitRect.contains(p.x, p.y)) {
            return lastHited!!
        }

        stageRoot.children.forEach {
            it.getHitRect(hitRect)
            if (hitRect.contains(p.x, p.y)) {
                lastHited = it
                return it
            }
        }

        return stageRoot
    }

//    private var direction = Point()
//    private var dragCell: DummyCell? = null
//    private var dragApp: AppView? = null
//    private var isFirstDrag = true

    override fun startDrag(v: View) {
//        if (v is AppView) {
//            v.startDrag(ClipData.newPlainText("",""), v.createDragShadow(), DragState(v, this), 0)
//        }
    }

    override fun onFocus(event: DragEvent) {
        // it's time to handle this drag startEvent
//        isFirstDrag = true
//        direction = Point(0, 0)
//
//        dragApp = if (isMyEvent(startEvent)) getParcelApp(startEvent) else createAppView(getParcelApp(startEvent).appInfo)
//        dragCell = if (isMyEvent(startEvent)) dragApp!!.parent as DummyCell else null
//
//        if (isMyEvent(startEvent)) {
//            // we have started the drag startEvent
//            removeApp(dragCell!!)
//        } else {
//            // drag becomes from other stage
//            adaptApp(dragApp!!) // we don't create a copy
//        }
//        trashView.activate()

    }

    override fun onFocusLost(event: DragEvent) {}

    override fun onDragEnded(event: DragEvent) {
//        dragCell = null
//        dragApp = null
//        trashView.deactivate()
//        saveState()

/*        cell.parentGrid.dragEnded()
        cell.parentGrid.saveState()
//      dragApp?.icon?.clearColorFilter()
        dragCell = null
        dragApp = null*/
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
//        super.onDrag(v, startEvent)
//
//        when (startEvent?.action) {
//
//            DragEvent.ACTION_DRAG_STARTED -> {}
//
//            DragEvent.ACTION_DRAG_ENTERED -> {
//                if (v is DummyCell)
//                    v.setBackgroundResource(R.drawable.bot_gradient)
//            }
//
//            DragEvent.ACTION_DRAG_LOCATION -> {
//                if (isFirstDrag) isFirstDrag = false else dragApp?.dismissMenu()
//
//                if (v is DummyCell) {
//                    val newDirection: Point =
//                        if (startEvent.y > startEvent.x)
//                            if (startEvent.y > v.height - startEvent.x) Point(0, -1) else Point(1, 0)
//                        else
//                            if (startEvent.y > v.height - startEvent.x) Point(-1, 0) else Point(0, 1)
//
//                    if (direction != newDirection) {
//                        // TODO: doTranslateBy is shit
//                        doTranslateBy(v, direction, 0f) // back translating
//                        direction = newDirection
//                        doTranslateBy(v, direction, 100f)
//                    }
//
//                    (v.parent as SnapGridLayout).tryFlipPage(v, startEvent)
//
//                    if (startEvent.y > v.top + FLIP_ZONE) {
//                        flipToStage(0, startEvent)
//                    }
//                }
//            }
//
//            DragEvent.ACTION_DRAG_EXITED -> {
//                if (v is DummyCell) {
//                    doTranslateBy(v, direction, 0f) // back translating
//                    v.defaultState()
//                }
//            }
//
//            DragEvent.ACTION_DROP -> {
//                // v is the view to drop
//                if (v == trashView) {
//                    dragApp = null
//                }
//                else if (v is DummyCell && canMoveBy(v, direction)) {
//                    doTranslateBy(v, direction, 0f) // back translating - just for prevent blinking
//                    doMoveBy(v, direction)
//                    putApp(dragApp!!, v)
//                }
//            }
//
//            DragEvent.ACTION_DRAG_ENDED -> {
//                if (v is DummyCell && !isEnded) {
//                    // will be called only once per drag startEvent
//                    isEnded = true
//                    // TODO: parentGrid, dragEnded, saveState are shits
//                    val grid = v.parent as SnapGridLayout
//                    grid.dragEnded()
//
////                    endDrag()
//                }
//                if (v is DummyCell)
//                    v.defaultState()
//            }
//        }
        return true
    }



/*    private fun putApp(app: AppView, to: DummyCell) {
        to.app = app
        apps.put(to.position, app.appInfo.id)
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

            apps.put(to.position, apps[from.position]!!)
            apps.remove(from.position)
        }
    }*/

//    private fun doRecursionPass(cell: DummyCell, direction: Point, action: (thisCell: DummyCell, nextCell: DummyCell) -> Unit): Boolean {
//        if (cell.isEmptyCell()) {
//            return true
//        }
//        if (direction.x == 0 && direction.y == 0) {
//            action(cell, cell)
//            return true
//        }
//        val next = Point(cell.relativePosition.x + direction.x, cell.relativePosition.y + direction.y)
//        val nextCell: DummyCell? = (cell.parent as SnapGridLayout).getCellAt(next)
//        if (nextCell != null && doRecursionPass(nextCell, direction, action)) {
//            action(cell, nextCell)
//            return true
//        }
//        return false
//    }
//
//    private fun canMoveBy(cell: DummyCell, direction: Point): Boolean {
//        return doRecursionPass(cell, direction) { thisCell, nextCell -> }
//    }
//
//    private fun doMoveBy(cell: DummyCell, direction: Point): Boolean {
//        return doRecursionPass(cell, direction) { thisCell, nextCell ->
//            moveApp(thisCell, nextCell)
//        }
//    }
//
//    private fun doTranslateBy(cell: DummyCell, direction: Point, value: Float): Boolean {
//        return doRecursionPass(cell, direction) { thisCell, nextCell ->
//            thisCell.app?.translationX = value*direction.x
//            thisCell.app?.translationY = value*direction.y
//        }
//    }
}