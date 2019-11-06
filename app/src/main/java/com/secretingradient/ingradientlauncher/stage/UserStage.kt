package com.secretingradient.ingradientlauncher.stage

import android.graphics.Color
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.FolderView
import com.secretingradient.ingradientlauncher.element.WidgetView
import com.secretingradient.ingradientlauncher.element.isElement
import com.secretingradient.ingradientlauncher.sensor.BaseSensor
import kotlinx.android.synthetic.main.stage_1_user.view.*
import kotlin.math.ceil
import kotlin.math.sqrt

class UserStage(launcherRootLayout: LauncherRootLayout) : BasePagerSnapStage(launcherRootLayout) {
    val FLIP_WIDTH = toPx(25).toInt()

    var apps = DataKeeper.userStageAppsData
    var folders = DataKeeper.userStageFoldersData
    override var columnCount = getPrefs(context).getInt(Preferences.USER_STAGE_COLUMN_COUNT, -1)
    override var rowCount = getPrefs(context).getInt(Preferences.USER_STAGE_ROW_COUNT, -1)
    override var pageCount = getPrefs(context).getInt(Preferences.USER_STAGE_PAGE_COUNT, -1)
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_1_user
    override val viewPagerId = R.id.user_stage_pager
    override val pagerAdapter = PagerSnapAdapter(apps, folders)
    val currentSnapLayout: SnapLayout
        get() = stageRV.getChildAt(0) as SnapLayout
    var shouldIntercept
        set(value) {stageRootLayout.shouldIntercept = value}
        get() = stageRootLayout.shouldIntercept
    private lateinit var touchHandler: TouchHandler
    private var sensors = mutableListOf<BaseSensor>()
    private val defaultAppSize = toPx(70).toInt()

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        touchHandler = TouchHandler()
        stageRootLayout.setOnTouchListener(touchHandler)
        stageRootLayout.preDispatchListener = object : OnPreDispatchListener {
            override fun onPreDispatch(event: MotionEvent) {
                touchHandler.preDispatch(event)
            }
        }

        stageRootLayout.apply {
            sensors.add(up_sensor.apply { sensorListener = touchHandler.upSensorListener})
            sensors.add(info_sensor)
            sensors.add(remove_sensor)
            sensors.add(uninstall_sensor)
        }

        touchHandler.hideSensors()

//        stageVP.offscreenPageLimit = 2
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TODO("remove this") //To change body of created functions use File | Settings | File Templates.
    }

    private inner class TouchHandler : View.OnTouchListener {
        var inEditMode = false
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
        var flipPageDone = false
        var folderPopup = PopupWindow(context).apply { isClippingEnabled = false }
        var wasMoveAfterStartEditMode = false  // crutch

        val upSensorListener = object : BaseSensor.SensorListener {
            override fun onSensor(v: View) {
                if (v is AppView)
                    transferEvent(v)
            }
            override fun onExitSensor() {}
            override fun onPerformAction(v: View) {}
        }

        init {
            gestureHelper.doOnLongClick = {
                startEditMode()
                if (selectedView != null) {
                    startDrag(selectedView!!, touchPoint)
                    wasMoveAfterStartEditMode = false
                }
            }
        }

        fun preDispatch(event: MotionEvent) {
            gestureHelper.onTouchEvent(event)

            if (event.action == MotionEvent.ACTION_DOWN) {
                // handle ACTION_DOWN for both inEditMode and !inEditMode
                disallowHScroll(false)
                if (isTouchOutsideFolder())
                    closeFolder()
                touchPoint.set(event.x.toInt(), event.y.toInt())
                selectedView = trySelect(findViewUnder(touchPoint))
                lastHoveredView = selectedView

                if (inEditMode) {
                    disallowVScroll()
                    if (selectedView != null)
                        startDrag(selectedView!!, touchPoint)
                }
            }

            if (gestureHelper.gesture == Gesture.TAP_UP && selectedView is FolderView && !inEditMode) {
                openFolder(selectedView as FolderView)
            }

            if (!wasMoveAfterStartEditMode && event.action == MotionEvent.ACTION_UP)
                endDrag()

            if (inEditMode && !disallowHScroll && selectedView == null)
                stageRV.onTouchEvent(event)
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (v !is StageRootLayout && !inEditMode)
                return false

            touchPoint.set(event.x.toInt(), event.y.toInt())

            when (event.action) {

                MotionEvent.ACTION_MOVE -> {
                    wasMoveAfterStartEditMode = true
                    if (selectedView != null) {

                        val hoveredView = findHoveredViewAt(touchPoint, lastHoveredView)
                        if (hoveredView is SnapLayout) {
                            movePosition = getPositionOnSnapUnder(hoveredView, touchPoint)
                        }
                        if (isNewHoveredView(hoveredView)) {
                            onExitHover(lastHoveredView)
                            onHover(selectedView!!, hoveredView, movePosition)
                        }
                        // if it is folder creation, then app was replaced to folder and we set lastHoveredView to folderView
                        lastHoveredView = if (selectedView is AppView && hoveredView is AppView) previewFolder else hoveredView
                        lastMovePosition = movePosition
                        flipPageIfNeeded(touchPoint)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (selectedView != null) {
                        val hoveredView = findHoveredViewAt(touchPoint, lastHoveredView)
                        if (hoveredView is SnapLayout) {
                            movePosition = getPositionOnSnapUnder(hoveredView, touchPoint)
                        }
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

        private fun flipPageIfNeeded(touchPoint: Point) {
            var direction = 0
            if (touchPoint.x > stageRootLayout.width - FLIP_WIDTH)
                direction = 1
            else if (touchPoint.x < FLIP_WIDTH)
                direction = -1

            if (direction != 0 && !flipPageDone)
                flipPage(direction)
            else if (direction == 0)
                flipPageDone = false
        }

        private fun flipPage(direction: Int) {
            stageVP.currentItem += direction
            cancelPreviewFolder()
            closeFolder()
            flipPageDone = true
        }

        private fun isNewHoveredView(hoveredView: View?): Boolean {
            return movePosition != lastMovePosition || hoveredView != lastHoveredView || lastHoveredView == null
        }

        private fun startDrag(selectedView: View, touchPoint: Point) {
            println("startDrag")
            stageRootLayout.overlayView = selectedView
            stageRootLayout.setOverlayTranslation(touchPoint.x.toFloat(), touchPoint.y.toFloat())
            selectedView.visibility = View.INVISIBLE
            disallowHScroll()
            lastMovePosition = -1
            lastHoveredView = null
            flipPageDone = false
        }

        private fun endDrag() {
            println("endDrag")
            selectedView?.visibility = View.VISIBLE
            stageRootLayout.overlayView = null
            selectedView = null
            cancelPreviewFolder()
            flipPageDone = false
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
                isElement(selectedView) && hoveredView is BaseSensor -> hoveredView.onSensor(selectedView)
                isElement(selectedView) && hoveredView is SnapLayout -> moveElement(ghostView, hoveredView, movePosition)
//                hoveredView == null -> { cancelPreviewFolder() }
            }
        }

        fun onPerformAction(selectedView: View, hoveredView: View, movePosition: Int) {
            when {
                selectedView is AppView && hoveredView is FolderView -> addToFolder(hoveredView, selectedView)
                isElement(selectedView) && hoveredView is BaseSensor -> hoveredView.onPerformAction(selectedView)
                isElement(selectedView) && hoveredView is SnapLayout -> moveElement(selectedView, hoveredView, movePosition)
            }
        }

        fun findHoveredViewAt(touchPoint: Point, lastHoveredView: View?): View? {
            // hovered view can't be neither selectedView nor ghostView
            val v = findViewUnder(touchPoint, lastHoveredView)
            return if (v != selectedView && v != ghostView) v else currentSnapLayout
        }

        fun moveElement(element: View, snapLayout: SnapLayout, movePosition: Int) {
            val parent = element.parent as ViewGroup?
            if (parent != snapLayout) {
                parent?.removeView(element)
                snapLayout.addView(element)
            }
            val from = (element.layoutParams as SnapLayout.SnapLayoutParams).position
            snapLayout.moveView(element, movePosition)
            DataKeeper2.onUserStageDataChangedListener.onMoved(context, from, movePosition)
        }

        private fun getPositionOnSnapUnder(snapLayout: SnapLayout, touchPoint: Point): Int {
            toLocationInView(touchPoint, snapLayout, reusablePoint)
            return snapLayout.snapToGrid(reusablePoint, 2)
        }

        fun createPreviewFolder(appView: AppView) {
            cancelPreviewFolder()
            val folder = FolderView(context, appView.appInfo)
            currentSnapLayout.removeView(appView)
            currentSnapLayout.addNewView(folder, (appView.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
            previewFolder = folder
        }

        private fun addToFolder(folder: FolderView, appView: AppView) {
            folder.addApps(appView.appInfo)
            (appView.parent as ViewGroup?)?.removeView(appView)
            previewFolder = null
        }

        fun cancelPreviewFolder() {
            previewFolder?.let {
                currentSnapLayout.removeView(it)
                val appView = it.getApp(0).createView(context)
                currentSnapLayout.addNewView(appView, (it.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
                lastHoveredView = appView
                it.clear()
                previewFolder = null
            }
        }

        fun openFolder(folder: FolderView) {
            closeFolder()
            setFolderContent(folder, folderPopup)
            folderPopup.showAsDropDown(folder, 0, -folder.height)
        }

        fun setFolderContent(folder: FolderView, folderPopup: PopupWindow) {
            // currently set Grid sizes 2 x 2 and 3 x 3
            val size = folder.folderSize
            var columnsCount = ceil(sqrt(size.toFloat())).toInt()
            if (columnsCount == 1) columnsCount = 2
            else if (columnsCount == 2 && size == 4) columnsCount = 3

            val appSize = defaultAppSize

            val grid = FolderLayout(context)
            grid.apps = folder.getApps()
            grid.appSize = appSize
            grid.columnsCount = columnsCount

            folderPopup.width = appSize * columnsCount
            folderPopup.height = appSize * columnsCount

            folderPopup.contentView = grid
        }

        fun closeFolder() {
            folderPopup.dismiss()
        }

        fun startEditMode() {
            inEditMode = true
            shouldIntercept = true
            closeFolder()
            disallowVScroll()
            disallowHScroll()
            showSensors(255/3)
        }

        fun endEditMode() {
            inEditMode = false
            shouldIntercept = false
            hideSensors()
        }

        fun isTouchOutsideFolder(): Boolean{
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

        fun showSensors(opacity: Int) {
            sensors.forEach {
                it.visibility = View.VISIBLE
                it.drawable.alpha = opacity
            }
        }

        fun hideSensors() {
            sensors.forEach {
                it.visibility = View.INVISIBLE
            }
        }

        fun transferEvent(appView: AppView) {
            launcherRootLayout.transferEvent(0, appView)
            currentSnapLayout.removeView(selectedView)
            endDrag()
            endEditMode()
        }

    }

}