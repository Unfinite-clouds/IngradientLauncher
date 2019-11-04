package com.secretingradient.ingradientlauncher.stage

import android.graphics.Color
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.element.*
import com.secretingradient.ingradientlauncher.sensor.BaseSensor

class UserStage(launcherRootLayout: LauncherRootLayout) : BasePagerSnapStage(launcherRootLayout) {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = DataKeeper.userStageAppsData
    var folders = DataKeeper.userStageFoldersData
    override var columnCount = getPrefs(context).getInt(Preferences.USER_STAGE_COLUMN_COUNT, -1)
    override var rowCount = getPrefs(context).getInt(Preferences.USER_STAGE_ROW_COUNT, -1)
    override var pageCount = getPrefs(context).getInt(Preferences.USER_STAGE_PAGE_COUNT, -1)
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_1_user
    override val viewPagerId = R.id.user_stage_pager
    override val pagerAdapter = PagerSnapAdapter(apps, folders)
    lateinit var trashView: TrashView
    val currentSnapLayout: SnapLayout
        get() = stageRV.getChildAt(0) as SnapLayout
    var shouldIntercept
        set(value) {stageRootLayout.shouldIntercept = value}
        get() = stageRootLayout.shouldIntercept
    private lateinit var userStageTouchEvent: UserStageTouchEvent

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        trashView = stageRootLayout.findViewById(R.id.trash_view2)
        userStageTouchEvent = UserStageTouchEvent()
        stageRootLayout.setOnTouchListener(userStageTouchEvent)
        stageRootLayout.preDispatchListener = object : OnPreDispatchListener {
            override fun onPreDispatch(event: MotionEvent) {
                userStageTouchEvent.preDispatch(event)
            }
        }
//        trashView.setOnTouchListener(this)
//        stageVP.offscreenPageLimit = 2
//        (stageVP.getChildAt(0) as ViewGroup).clipChildren = false
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TODO("remove this") //To change body of created functions use File | Settings | File Templates.
    }

    private inner class UserStageTouchEvent : View.OnTouchListener {
        var folderPopup: PopupWindow? = null
        var inEditMode = false
        val sensorInfo = BaseSensor(context)
        val sensorRemove = BaseSensor(context)
        val sensorUninstall = BaseSensor(context)
        val sensorUp = BaseSensor(context)
        val sensorLeft = BaseSensor(context)
        val sensorRight = BaseSensor(context)
        var selectedView: View? = null
        var lastHoveredView: View? = null
        val touchPoint = Point()
        val reusablePoint = Point()
        val ghostView = ImageView(context).apply { setBackgroundColor(Color.LTGRAY); layoutParams = SnapLayout.SnapLayoutParams(0, 2, 2) }
        val gestureHelper = GestureHelper(context)
        var disallowHScroll = false
        var movePosition = -1
        var lastMovePosition = -1
        var previewFolder: FolderView? = null

        init {
            gestureHelper.doOnLongClick = { startEditMode(); if (selectedView != null) startDrag(selectedView!!) }
        }

        fun preDispatch(event: MotionEvent) {
            gestureHelper.onTouchEvent(event)

            if (event.action == MotionEvent.ACTION_DOWN) {
                // for both inEditMode and !inEditMode
                disallowHScroll(false)
                if (isTouchOutsideFolder())
                    closeFolder()
                touchPoint.set(event.x.toInt(), event.y.toInt())
                selectedView = trySelect(findViewUnder(touchPoint))
                lastHoveredView = selectedView

                if (inEditMode) {
                    disallowVScroll()
                    if (selectedView != null)
                        startDrag(selectedView!!)
                }
            }

            if (shouldIntercept && !disallowHScroll && selectedView == null)
                stageRV.onTouchEvent(event)
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (v !is StageRootLayout && !inEditMode)
                return false

            touchPoint.set(event.x.toInt(), event.y.toInt())

            when (event.action) {

                MotionEvent.ACTION_MOVE -> {
                    if (selectedView != null) {

                        val hoveredView = findHoveredViewAt(touchPoint, lastHoveredView)
                        if (hoveredView is SnapLayout)
                            movePosition = getTouchPositionOnSnap(hoveredView, touchPoint)
                        if (isNewHoveredView(hoveredView)) {
                            onExitHover(lastHoveredView)
                            onHover(selectedView!!, hoveredView, movePosition)
                        }
                        // if it is folder creation, then app was replaced to folder, we set lastHoveredView to folderView
                        lastHoveredView = if (selectedView is AppView && hoveredView is AppView) previewFolder else hoveredView
                        lastMovePosition = movePosition
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (selectedView != null) {
                        val hoveredView = findHoveredViewAt(touchPoint, lastHoveredView)
                        if (hoveredView != null) {
                            onPerformAction(selectedView!!, hoveredView, movePosition)
                            onExitHover(hoveredView)
                        }
                        lastHoveredView = hoveredView
                        lastMovePosition = movePosition
                    } else if (gestureHelper.gesture == Gesture.TAP_UP) // not after longPress
                        endEditMode()
                    endDrag()
                }

            }

            return true
        }

        private fun isNewHoveredView(hoveredView: View?): Boolean {
            return movePosition != lastMovePosition || hoveredView != lastHoveredView || lastHoveredView == null
        }

        private fun startDrag(selectedView: View) {
            println("startDrag")
            stageRootLayout.overlayView = selectedView
            selectedView.visibility = View.INVISIBLE
            disallowHScroll()
            lastMovePosition = -1
            lastHoveredView = null
        }

        private fun endDrag() {
            println("endDrag")
            selectedView?.visibility = View.VISIBLE
            stageRootLayout.overlayView = null
            selectedView = null
            cancelPreviewFolder()
        }

        fun onExitHover(view: View?) {
            when (view) {
                is BaseSensor -> view.onExitSensor()
                is FolderView -> cancelPreviewFolder()
                is SnapLayout -> view.removeView(ghostView)
            }
        }

        fun onHover(selectedView: View, hoveredView: View?, movePosition: Int) {
            when {
                selectedView is AppView && hoveredView is AppView -> createPreviewFolder(hoveredView)
                isElement(selectedView) && hoveredView is BaseSensor -> hoveredView.onSensored(selectedView)
                isElement(selectedView) && hoveredView is SnapLayout -> moveGhostView(hoveredView, movePosition)
                hoveredView == null -> { cancelPreviewFolder() }
            }
        }

        fun onPerformAction(selectedView: View, hoveredView: View, movePosition: Int) {
            when {
                selectedView is AppView && hoveredView is FolderView -> { hoveredView.addApps(selectedView); previewFolder = null }
                isElement(selectedView) && hoveredView is BaseSensor -> hoveredView.onPerformAction(selectedView)
                isElement(selectedView) && hoveredView is SnapLayout -> hoveredView.moveView(selectedView, movePosition)
            }
        }

        fun findHoveredViewAt(touchPoint: Point, lastHoveredView: View?): View? {
            // hovered view can't be neither selectedView nor ghostView
            val v = findViewUnder(touchPoint, lastHoveredView)
            return if (v != selectedView && v != ghostView) v else currentSnapLayout
        }

        fun moveElement(element: View, snapLayout: SnapLayout, touchPoint: Point) {
            val pos = getTouchPositionOnSnap(snapLayout, touchPoint)
            snapLayout.moveView(element, pos)
        }

        fun moveGhostView(snapLayout: SnapLayout, movePosition: Int) {
            if (ghostView.parent != snapLayout) {
                (ghostView.parent as? ViewGroup)?.removeView(ghostView)
                snapLayout.addView(ghostView)
            }
            snapLayout.moveView(ghostView, movePosition)
        }

        private fun getTouchPositionOnSnap(snapLayout: SnapLayout, touchPoint: Point): Int {
            toLocationInView(touchPoint, snapLayout, reusablePoint)
            return snapLayout.snapToGrid(reusablePoint, 2)
        }

        fun createPreviewFolder(appView: AppView) {
            cancelPreviewFolder()
            val folder = FolderView(context, appView)
            currentSnapLayout.removeView(appView)
            currentSnapLayout.addNewView(folder, (appView.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
            previewFolder = folder
        }

        fun cancelPreviewFolder() {
            previewFolder?.let {
                currentSnapLayout.removeView(it)
                currentSnapLayout.addNewView(it[0], (it.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
                it.clear()
                previewFolder = null
            }
        }

        fun startEditMode() {
            inEditMode = true
            shouldIntercept = true
            closeFolder()
            disallowVScroll()
            disallowHScroll()
            println("startEditMode")
        }

        fun endEditMode() {
            inEditMode = false
            shouldIntercept = false
            println("endEditMode")
        }

        fun openFolder(folder: FolderView) {
            // todo
        }

        fun closeFolder() {
            folderPopup?.dismiss()
            folderPopup = null
        }

        fun isTouchOutsideFolder(): Boolean{
            // todo
            return true
        }

        fun trySelect(v: View?): View? {
            if (!isElement(v))
                return null
            when (v) {
                is AppView -> onAppSelected(v)
                is FolderView -> onFolderSelected(v)
                is WidgetView -> onWidgetSelected(v)
            }
            return v
        }

        fun onAppSelected(v: View) {/*todo*/}
        fun onFolderSelected(v: View) {/*todo*/}
        fun onWidgetSelected(v: View) {/*todo*/}

        fun disallowHScroll(disallow: Boolean = true) {
            disallowHScroll = disallow
        }
    }

}