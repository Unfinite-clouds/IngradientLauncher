package com.secretingradient.ingradientlauncher.stage

import android.graphics.Color
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
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

//    private val editModeListener = EditModeListener()
//    private val editModeDetector = GestureDetector(context, editModeListener)
//    private val touchListener = UserStageTouchEvent()

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        trashView = stageRootLayout.findViewById(R.id.trash_view2)
        stageRootLayout.setOnTouchListener(userStageTouchEvent)
        stageRootLayout.preDispatchListener = onPreDispatch
//        trashView.setOnTouchListener(this)
//        stageVP.offscreenPageLimit = 2
//        (stageVP.getChildAt(0) as ViewGroup).clipChildren = false
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TODO("remove this") //To change body of created functions use File | Settings | File Templates.
    }

    val onPreDispatch = object : OnPreDispatchListener {
        override fun onPreDispatch(event: MotionEvent) {
            userStageTouchEvent.preDispatch(event)
        }
    }
    
    private val userStageTouchEvent = object : View.OnTouchListener, View.OnLongClickListener {
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
        val ghostView = ImageView(context).apply { setBackgroundColor(Color.LTGRAY); layoutParams = SnapLayout.SnapLayoutParams(-1,2,2) }
        val gestureHelper = GestureHelper(context)

        fun preDispatch(event: MotionEvent) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                touchPoint.set(event.x.toInt(), event.y.toInt())
                selectedView = trySelect(findViewAt(touchPoint))

                if (isTouchOutsideFolder())
                    closeFolder()
            }
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (v !is StageRootLayout && !inEditMode)
                return false

            gestureHelper.onTouchEvent(event)

            touchPoint.set(event.x.toInt(), event.y.toInt())

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    if (isTouchOutsideFolder())
                        closeFolder()
                    val touchedView = findViewAt(touchPoint)
                    selectedView = trySelect(touchedView)
                    lastHoveredView = touchedView
                }

                MotionEvent.ACTION_MOVE -> {
                    if (selectedView != null) {
                        val hoveredView = findViewAt(touchPoint, lastHoveredView)
                        if (hoveredView != lastHoveredView && hoveredView != null) {
                            if (lastHoveredView != null)
                                onExitHover(lastHoveredView!!)
                            onHover(selectedView!!, hoveredView, touchPoint)
                        }
                        lastHoveredView = hoveredView
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (selectedView != null) {
                        val hoveredView = findViewAt(touchPoint, lastHoveredView)
                        if (hoveredView != null) {
                            onPerformAction(selectedView!!, hoveredView, touchPoint)
                        }
                        lastHoveredView = hoveredView
                    }
                    if (gestureHelper.gesture == Gesture.TAP_UP) // not after longPress
                        endEditMode()
                }

            }



            return true
        }

        fun onExitHover(lastHoveredView: View) {
            when (lastHoveredView) {
                is BaseSensor -> lastHoveredView.onExitSensor()
                is FolderView -> previewCancelFolder(lastHoveredView)
            }
        }

        fun onHover(selectedView: View, hoveredView: View, touchPoint: Point) {
            when {
                selectedView is AppView && hoveredView is AppView -> previewCreateFolder(hoveredView)
                isElement(selectedView) && hoveredView is BaseSensor -> hoveredView.onSensored(selectedView)
                isElement(selectedView) && hoveredView is SnapLayout -> moveElement(ghostView, hoveredView, touchPoint)
            }
        }

        fun onPerformAction(selectedView: View, hoveredView: View, touchPoint: Point) {
            when {
                selectedView is AppView && hoveredView is FolderView -> hoveredView.addApps(selectedView)
                isElement(selectedView) && hoveredView is BaseSensor -> hoveredView.onPerformAction(selectedView)
                isElement(selectedView) && hoveredView is SnapLayout -> moveElement(selectedView, hoveredView, touchPoint)
            }
        }

        fun moveElement(element: View, snapLayout: SnapLayout, touchPoint: Point) {
            val pos = getTouchPositionOnSnap(snapLayout, touchPoint)
            snapLayout.moveView(element, pos)
        }

        fun previewCreateFolder(appView: AppView): FolderView {
            val folder = FolderView(context, appView)
            currentSnapLayout.removeView(appView)
            currentSnapLayout.addNewView(folder, (appView.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
            return folder
        }

        fun previewCancelFolder(lastHoveredView: FolderView) {
            currentSnapLayout.removeView(lastHoveredView)
            currentSnapLayout.addNewView(lastHoveredView[0], (lastHoveredView.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
            lastHoveredView.clear()
        }

        override fun onLongClick(v: View?): Boolean {
            closeFolder()
            startEditMode()
            return true
        }

        fun startEditMode() {
            inEditMode = true
            disallowVScroll()
            shouldIntercept = true
            println("startEditMode")
        }

        fun endEditMode() {
            inEditMode = false
            shouldIntercept = false
            println("endEditMode")
        }

        fun removeElement(element: Element) {
            // todo
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
            disallowHScroll()
            when (v) {
                is AppView -> onAppSelected(v)
                is FolderView -> onFolderSelected(v)
                is WidgetView -> onWidgetSelected(v)
            }
            return v
        }

        private fun getTouchPositionOnSnap(snapLayout: SnapLayout, touchPoint: Point): Int {
            toLocationInView(touchPoint, snapLayout, reusablePoint)
            return snapLayout.snapToGrid(reusablePoint, 2)
        }

        fun onAppSelected(v: View) {/*todo*/}
        fun onFolderSelected(v: View) {/*todo*/}
        fun onWidgetSelected(v: View) {/*todo*/}

        fun disallowHScroll(disallow: Boolean = true) {
            /*todo?*/
        }

        fun isElement(v: View?): Boolean {
            return v as? AppView ?: v as? FolderView ?: v as? WidgetView != null
        }
    }

}