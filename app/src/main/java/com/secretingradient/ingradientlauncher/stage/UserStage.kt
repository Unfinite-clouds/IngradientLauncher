package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.view.*
import android.widget.ImageView
import androidx.core.view.children
import androidx.core.view.setPadding
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.element.*

class UserStage(context: Context) : BasePagerSnapStage(context) {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = AppManager.customGridApps
    var rowCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_ROW_COUNT, -1)
    var columnCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_COLUMN_COUNT, -1)
    var pageCount = getPrefs(context).getInt(Preferences.CUSTOM_GRID_PAGE_COUNT, -1)
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_1_custom_grid
    override val viewPagerId = R.id.user_stage_pager
    override val pagerAdapter = UserPagerAdapter(context) as PagerSnapAdapter
    lateinit var trashView: TrashView
    val currentSnapLayout: SnapLayout
        get() = (stageViewPager.getChildAt(0) as ViewGroup).getChildAt(0) as SnapLayout

    override fun inflateAndAttach(stageRoot: StageRoot) {
        super.inflateAndAttach(stageRoot)
        trashView = stageRoot.findViewById(R.id.trash_view)
//        trashView.setOnDragListener(this)
        stageRoot.setOnTouchListener(this)
        trashView.setOnTouchListener(this)
        (stageViewPager.getChildAt(0) as ViewGroup).clipChildren = false
    }

    inner class UserPagerAdapter(context: Context) : BasePagerSnapStage.PagerSnapAdapter(context, columnCount, rowCount) {
        override fun getItemCount() = pageCount

        override fun createPage(page: Int): SnapLayout {
            val snapLayout = SnapLayout(context, columnCount*2, rowCount*2) // do nothing

//            var appInfo: AppInfo?
//            var i = 0
//            apps.forEach {
//                appInfo = AppManager.getApp(it.value) ?: throw LauncherException()
//                snapLayout.addView(createAppView(appInfo!!), SnapLayout.SnapLayoutInfo(i*2 + (i*2/8)*8, 2, 2))
//                if (i>5)
//                    return@forEach
//                i++
//            }

            val pageState = MutableList(8) {i ->
                SnapElementInfo(AppManager.getApp(apps[i]!!)!!, SnapLayout.SnapLayoutInfo((i + (i/columnCount)*columnCount)*2, 2, 2))
            }
            pageStates.add(pageState)

            return snapLayout
        }
    }

    fun createAppView(appInfo: AppInfo): AppView {
        return AppView(context, appInfo).apply { adaptApp(this) }
    }

    override fun adaptApp(app: AppView) {
        app.setPadding(cellPadding)
    }

    val gListener = GestureListener()
    val gDetector = GestureDetector(context, gListener)
    private var selected: AppView? = null
    private var inEditMode = false
    private val ghostView = ImageView(context).apply { setBackgroundColor(Color.LTGRAY) }
    private val pointer = PointF()
    private val touchPoint = Point()
    private val scaleInEditMode = 0.85f
    private val eventPoint = Point()

    private fun startEditMode() {
        inEditMode = true
        stageRoot.parent.requestDisallowInterceptTouchEvent(true)
        stageViewPager.animate().scaleX(scaleInEditMode).scaleY(scaleInEditMode).start()
    }

    private fun endEditMode() {
        if (inEditMode) {
            selected?.let {
                it.translationX = 0f
                it.translationY = 0f
            }
            selected = null
            stageViewPager.animate().scaleX(1f).scaleY(1f).start()
        }
        inEditMode = false
//        stageRoot.parent.requestDisallowInterceptTouchEvent(false)
        stageRoot.shouldIntercept = false
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        gDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                println("ACTION_DOWN --- x = ${event.x} y = ${event.y} selected = ${selected?.javaClass?.simpleName}, v = ${v.javaClass.simpleName}")
                selected = v as? AppView
                if (selected != null) {
                    touchPoint.set(selected!!.left + stageViewPager.left + event.x.toInt(), selected!!.top + stageViewPager.top + event.y.toInt())
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

            MotionEvent.ACTION_MOVE -> {
                println("ACTION_MOVE x = ${event.x} y = ${event.y} selected = ${selected?.javaClass?.simpleName}, v = ${v.javaClass.simpleName}")

                // TODO: test for perform fast swipe
//                if (isSwipe) ...
                if (selected == null || !inEditMode)
                    return false

                selected!!.translationX = (event.x - touchPoint.x)/scaleInEditMode
                selected!!.translationY = (event.y - touchPoint.y)/scaleInEditMode


                eventPoint.set(event.x.toInt(), event.y.toInt())
                val hitedView = getHitView(eventPoint)

                when (hitedView) {
                    is TrashView -> hitedView.activate()
                    is ViewPager2 -> {
                        val snap = currentSnapLayout
                        ghostView.layoutParams = SnapLayout.SnapLayoutParams(-1, 2, 2)
                        snap.removeView(ghostView)
                        snap.tryAddView(ghostView, ghostView.layoutParams as SnapLayout.SnapLayoutParams, eventPoint)
                    }
                }
                (hitedView as? TrashView)?.activate()

            }

            MotionEvent.ACTION_UP -> {
                println("ACTION_UP selected = ${selected?.javaClass?.simpleName}, v = ${v.javaClass.simpleName}")
                selected = null
                stageRoot.shouldIntercept = false
            }
        }

        return true
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            startEditMode()
        }
    }

    private var lastHited: View? = null
    private val hitRect = Rect()
//    private val p = Point()
    private var hitedView: View? = null

    private fun getHitView(p: Point): View {

        lastHited?.getHitRect(hitRect)
        if (lastHited != null && hitRect.contains(p.x, p.y)) {
            return lastHited!!
        }

        stageRoot.children.forEach {
            it.getHitRect(hitRect)
            if (hitRect.contains(p.x, p.y))
                return it
        }

        return stageRoot
    }

/*    private fun testHit(v: View): Boolean {
        v.getHitRect(hitRect)
        return hitRect.contains(p.x, p.y)
    }*/





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