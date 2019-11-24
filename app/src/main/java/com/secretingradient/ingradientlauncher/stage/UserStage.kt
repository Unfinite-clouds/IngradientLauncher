package com.secretingradient.ingradientlauncher.stage

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.graphics.Color
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.data.*
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.FolderView
import com.secretingradient.ingradientlauncher.element.WidgetView
import com.secretingradient.ingradientlauncher.sensor.BaseSensor
import com.secretingradient.ingradientlauncher.sensor.UpSensor
import kotlinx.android.synthetic.main.stage_1_user.view.*
import kotlin.math.ceil
import kotlin.math.min

class UserStage(launcherRootLayout: LauncherRootLayout) : BasePagerSnapStage(launcherRootLayout) {
    private val FLIP_WIDTH = toPx(25)
    private val defaultAppSize = toPx(70)

    override val stageLayoutId = R.layout.stage_1_user
    override val viewPagerId = R.id.user_stage_pager

    override var columnCount = getPrefs(context).getInt(Preferences.USER_STAGE_COLUMN_COUNT, -1)
    override var rowCount = getPrefs(context).getInt(Preferences.USER_STAGE_ROW_COUNT, -1)
    override var pageCount = getPrefs(context).getInt(Preferences.USER_STAGE_PAGE_COUNT, -1)

    override val pagerAdapter = PagerSnapAdapter()
    val dataset: Dataset<Data, Info> = dataKeeper.userStageDataset
    val currentSnapLayout: SnapLayout
        get() = stageRV.getChildAt(0) as SnapLayout
    var shouldIntercept
        set(value) {stageRootLayout.shouldIntercept = value}
        get() = stageRootLayout.shouldIntercept
    private lateinit var touchHandler: TouchHandler
    private var sensors = mutableListOf<BaseSensor>()
    val currentPage: Int
        get() = stageVP.currentItem

    lateinit var folderWindow: FolderWindow
    var isFolderOpen = false
    lateinit var upSensor: UpSensor
    val resizeFrame = View.inflate(context, R.layout.resize_frame, null) as WidgetResizeFrame
    var transferringWidgetPreview: WidgetPreview? = null

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
            sensors.add(remove_sensor.apply { sensorListener = touchHandler.removeSensorListener})
            sensors.add(uninstall_sensor)
        }
        upSensor = sensors[0] as UpSensor
        touchHandler.hideSensors()

        folderWindow = stageRootLayout.findViewById(R.id.folder_window)
        folderWindow.initData(dataset, defaultAppSize)

        stageRootLayout.clipChildren = false
        launcher.dragLayer.addView(resizeFrame)
        resizeFrame.visibility = View.GONE
//        stageVP.offscreenPageLimit = 2
    }

    override fun bindPage(holder: SnapLayoutHolder, page: Int) {
        dataset.forEach {
            if (isPosInPage(it.key, page)) {
                holder.snapLayout.addView(it.value.createView(context) // avoid creating here
                    .apply { setSnapLayoutParams(it.key % pageSize, it.value.snapWidth, it.value.snapHeight)}) // bad way
            }
        }
    }

    override fun receiveTransferEvent(obj: Any?) {
        println("receive $obj")
        if (obj is AppView) {
            launcherRootLayout.dispatchToCurrentStage = true
            touchHandler.receiveTransferringApp(obj)
        }
        else if (obj is WidgetPreviewInfo) {
            launcherRootLayout.dispatchToCurrentStage = true
            touchHandler.receiveTransferringWidget(obj)
        }
    }

    fun onAddWidget(widgetInfo: AppWidgetProviderInfo, widgetId: Int) {
        println("adding widget...")
        widgetInfo.apply {
            println("min = $minWidth, $minHeight, $minResizeWidth, $minResizeHeight")
        }
        if (transferringWidgetPreview == null) throw LauncherException("transferringWidgetPreview must not be null")
        (transferringWidgetPreview!!.parent as? ViewGroup)?.removeView(transferringWidgetPreview)
        val lp = transferringWidgetPreview!!.layoutParams as SnapLayout.SnapLayoutParams
        val widget = WidgetView(context, WidgetInfo(widgetInfo, widgetId, lp.snapWidth, lp.snapHeight))
        widget.layoutParams = lp
        touchHandler.select(widget)
        currentSnapLayout.addView(widget)
        dataset.put(lp.position, widget.widgetInfo)
        transferringWidgetPreview = null
        startResizeWidget(widget)
    }

    fun startResizeWidget(widget: WidgetView) {
        println("start Resize Widget")
        resizeFrame.visibility = View.VISIBLE
        resizeFrame.attachToWidget(widget)
    }

    override fun onStageAttachedToWindow() {
        if (touchHandler.isTransferring && touchHandler.selectedView != null) {
            touchHandler.startEditMode()
            touchHandler.startDragSelected()
        }
    }

    private inner class TouchHandler : View.OnTouchListener {
        var inEditMode = false
        val selectedHolder = SnapLayout.ElementHolder()
        val selectedView
            get() = selectedHolder.view
        var fromPosition = -1
        var lastHoveredView: View? = null
        val touchPoint = Point()
        val reusablePoint = Point()
        val ghostHolder = SnapLayout.ElementHolder(ImageView(context).apply { setBackgroundColor(Color.LTGRAY) }, -1, 0, 0)
        var disallowHScroll = false
        var layoutPosition = -1
        var lastLayoutPosition = -1
        var previewFolder: FolderView? = null
        var flipPageDone = false
        var wasMoveAfterStartEditMode = false  // crutch
        var isTouchInFolder = false
        var needToStartDragInFolder = false
        var isTransferring = false
        var isDrag = false

        val upSensorListener = object : BaseSensor.SensorListener {
            override fun onSensor(v: View) {
                if (v is AppView)
                    transferEvent(v)
            }
            override fun onExitSensor() {}
            override fun onPerformAction(v: View) {}
        }
        val removeSensorListener = object : BaseSensor.SensorListener {
            override fun onSensor(v: View) {}
            override fun onExitSensor() {}
            override fun onPerformAction(v: View) {
                removeView(v)
            }
        }

        init {
            gestureHelper.doOnLongClick = { downEvent ->
                startEditMode()
                if (isFolderOpen && downEvent != null) {
                    needToStartDragInFolder = true
                }
                if (!isFolderOpen && selectedHolder.view != null) {
                    startDragSelected()
                }
            }
        }

        fun preDispatch(event: MotionEvent) {
            gestureHelper.onTouchEvent(event)

            if (event.action == MotionEvent.ACTION_DOWN) {
                upSensor.disabled = false
                needToStartDragInFolder = false
                touchPoint.set(event.x.toInt(), event.y.toInt())
                if (isTouchOutsideFolder(touchPoint))
                    closeFolder()
                else
                    disallowHScroll()
                select(findInnerViewUnder(touchPoint))
                lastHoveredView = selectedView
            }

            if (gestureHelper.gesture == Gesture.TAP_UP && selectedView is FolderView) {
                openFolder(selectedView as FolderView)
            }

            if (!wasMoveAfterStartEditMode && event.action == MotionEvent.ACTION_UP) {
                cancelDrag()
                endDrag()
            }

            if (inEditMode && !disallowHScroll && selectedView == null)
                stageRV.onTouchEvent(event)
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            // only called by stageRootLayout in EditMode, intercepting TouchEvent
            if (v !is StageRootLayout || !inEditMode || launcherRootLayout.isAnimating)
                return true

            touchPoint.set(event.x.toInt(), event.y.toInt())
            launcher.dragLayer.onTouchEvent(event)

            if (!isTouchOutsideFolder(touchPoint)) {
                select(null)
                isTouchInFolder = true
                if (needToStartDragInFolder) {
                    needToStartDragInFolder = false
                    startDragInFolder(event)
                }
                else
                    dispatchWithTransform(folderWindow, event)
            } else if (isTouchInFolder) {
                isTouchInFolder = false
                receiveFromFolder(folderWindow, event)
            }

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    disallowVScroll()
                }

                MotionEvent.ACTION_MOVE -> {
                    wasMoveAfterStartEditMode = true
                    if (selectedView != null) {
                        if (!isDrag) // (when not in drag)
                            startDragSelected()
                        val hoveredView = findHoveredViewAt(touchPoint, lastHoveredView)
//                        println("${touchPoint.y}, ${hoveredView.className()}, ${lastHoveredView.className()}, ${upSensor.disabled}")
                        setLayoutPositionIfNeeded(hoveredView, touchPoint)
                        if (isNewHoveredView(hoveredView)) {
                            onExitHover(lastHoveredView)
                            onHover(selectedView!!, hoveredView, layoutPosition)
                        }
                        // if it is folder creation, then app was replaced to folder and we set lastHoveredView to folderView
                        lastHoveredView = if (selectedView is AppView && hoveredView is AppView) previewFolder else hoveredView
                        lastLayoutPosition = layoutPosition
                        flipPageIfNeeded(touchPoint)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (selectedView != null) {
                        val hoveredView = findHoveredViewAt(touchPoint, lastHoveredView)
                        setLayoutPositionIfNeeded(hoveredView, touchPoint)
                        if (hoveredView != null) {
                            onPerformAction(selectedView!!, hoveredView, layoutPosition)
                            onExitHover(hoveredView)
                        }
                        lastHoveredView = hoveredView
                        lastLayoutPosition = layoutPosition
                    }
                    if (gestureHelper.gesture == Gesture.TAP_UP && !isFolderOpen) {// not after longPress
                        endEditMode()
                    }
                    endDrag()
                }

            }

            return true
        }

        fun setLayoutPositionIfNeeded(hoveredView: View?, touchPoint: Point) {
            if (hoveredView is SnapLayout) {
                val p = Point(selectedView!!.x.toInt(), selectedView!!.y.toInt())
//                getLocationOfViewGlobal(selectedView!!, p)
                layoutPosition = getPositionOnSnapUnder(hoveredView, p)
            }
        }

        fun findHoveredViewAt(touchPoint: Point, lastHoveredView: View?): View? {
            // hovered view can't be neither selectedView nor ghostView
            val v = findInnerViewUnder(touchPoint, lastHoveredView)
            return if (v != selectedView && v != ghostHolder.view) v else currentSnapLayout
        }

        fun isNewHoveredView(hoveredView: View?): Boolean {
            return layoutPosition != lastLayoutPosition || hoveredView != lastHoveredView || lastHoveredView == null
        }

        fun startDragSelected() {
            val selectedView = selectedView ?: throw LauncherException("nothing is selected")
            println("-- startDrag --")
            isDrag = true
            moveToDragLayer(selectedView)
            disallowHScroll()
            disallowVScroll()
            ghostHolder.setLayoutParams(selectedView.layoutParams as SnapLayout.SnapLayoutParams)
            lastLayoutPosition = -1
            lastHoveredView = null
            flipPageDone = false
        }

        fun endDrag() {
            println("endDrag")
            isDrag = false
            removeFromDragLayer(selectedView)
            select(null)
            disallowHScroll(false)
            cancelPreviewFolder()
            flipPageDone = false
            isTransferring = false
        }

        fun startEditMode() {
            inEditMode = true
            shouldIntercept = true
            folderWindow.inEditMode = true
            wasMoveAfterStartEditMode = false
            disallowVScroll()
            disallowHScroll()
            showSensors(255/2)
        }

        fun endEditMode() {
            inEditMode = false
            folderWindow.inEditMode = false
            shouldIntercept = false
            resizeFrame.detachFromWidget()  // todo not here. need to do it on singleTap
            resizeFrame.visibility = View.GONE
            hideSensors()
        }

        fun transferEvent(appView: AppView) {
            if (selectedView != null) {
                println("transferEvent")
                launcherRootLayout.transferEvent(0, appView)
                removeView(selectedView!!)
                endDrag()
                endEditMode()
            }
        }

        fun receiveTransferringApp(appView: AppView) {
            isTransferring = true
            val lp = SnapLayout.SnapLayoutParams(-1, 2, 2, currentSnapLayout)
            selectedHolder.view = appView.info!!.createView(context).apply { layoutParams = lp }
            upSensor.disabled = true
        }

        fun receiveTransferringWidget(previewInfo: WidgetPreviewInfo) {
            isTransferring = true
            val snapLayout = currentSnapLayout
            val snapWidth = min(ceil(previewInfo.widgetInfo.minWidth.toFloat()/snapLayout.snapStepX).toInt(), snapLayout.width)
            val snapHeight = min(ceil(previewInfo.widgetInfo.minHeight.toFloat()/snapLayout.snapStepY).toInt(), snapLayout.height)
            val w = snapWidth*snapLayout.snapStepX
            val h = snapHeight*snapLayout.snapStepY
            val lp = SnapLayout.SnapLayoutParams(-1, snapWidth, snapHeight, snapLayout)
            transferringWidgetPreview = WidgetPreview(context, previewInfo)
                .apply { layoutParams = lp }
            select(transferringWidgetPreview)
            println("snaps = $snapWidth, $snapHeight, size = $w, $h, lp = ${lp.width}, ${lp.height}")
        }

        fun receiveTransferringWidget2(widget: AppWidgetHostView, widgetInfo: AppWidgetProviderInfo) {
            val snapLayout = currentSnapLayout
            val snapWidth = min(ceil(widgetInfo.minWidth.toFloat()/snapLayout.snapStepX).toInt(), snapLayout.width)
            val snapHeight = min(ceil(widgetInfo.minHeight.toFloat()/snapLayout.snapStepY).toInt(), snapLayout.height)
            val w = snapWidth*snapLayout.snapStepX
            val h = snapHeight*snapLayout.snapStepY
            widget.updateAppWidgetSize(null, w, h, w, h)  // todo make for landscape later
            isTransferring = true
            val lp = SnapLayout.SnapLayoutParams(-1, snapWidth, snapHeight, snapLayout)
            select(widget, lp)
            println("snaps = $snapWidth, $snapHeight, size = $w, $h, lp = ${lp.width}, ${lp.height}")
        }

        fun onExitHover(view: View?) {
            when (view) {
                is BaseSensor -> view.onExitSensor()
                is FolderView -> cancelPreviewFolder()
                is SnapLayout -> view.removeView(ghostHolder.view)
            }
        }

        fun onHover(selectedView: View, hoveredView: View?, movePosition: Int) {
            if (isTransferring && (isElement(hoveredView) || hoveredView is SnapLayout))
                upSensor.disabled = false
            when {
                selectedView is AppView && hoveredView is AppView -> createPreviewFolder(hoveredView)
                isElement(selectedView) && hoveredView is BaseSensor -> hoveredView.onSensor(selectedView)
                isElement(selectedView) && hoveredView is SnapLayout -> moveGhostView(hoveredView, movePosition)
                else -> println("no space")
//                hoveredView == null -> { cancelPreviewFolder() }
            }
        }

        fun onPerformAction(selectedView: View, hoveredView: View, movePosition: Int) {
            println("onPerformAction ${selectedView.className()}")
            when {
                selectedView is AppView && hoveredView is FolderView -> { addToFolder(hoveredView, selectedView); removeView(selectedView) }
                selectedView is WidgetPreview && hoveredView is SnapLayout -> startAddWidget(selectedView, movePosition)
                isElement(selectedView) && hoveredView is BaseSensor -> hoveredView.onPerformAction(selectedView)
                isElement(selectedView) && hoveredView is SnapLayout -> moveOrAddView(selectedView, hoveredView, movePosition)
                else -> cancelDrag()
            }
        }

        fun moveGhostView(snapLayout: SnapLayout, toLayoutPosition: Int) {
            val ghostView = ghostHolder.view ?: throw LauncherException("ghostView == null")
            if (!snapLayout.canPlaceHere(toLayoutPosition, ghostHolder.snapWidth, ghostHolder.snapHeight)) {
                println("no empty space")
                return
            }
            ghostHolder.snapPosition = toLayoutPosition
            val parent = ghostView.parent as ViewGroup?
            if (parent != snapLayout) {
                parent?.removeView(ghostView)
                snapLayout.addView(ghostView)
            }
//            ghostHolder.snapPosition = layoutPosition
//            if (snapLayout.canPlaceView(ghostView, ghostView))
//                snapLayout.moveView(ghostView, layoutPosition)
//            else
//                println("no empty space")
        }

        fun moveOrAddView(element: View, toSnapLayout: SnapLayout, toLayoutPosition: Int) {
            println("moveOrAddView ${selectedView.className()}, from = $fromPosition")
            if (element == ghostHolder.view) throw LauncherException("for ghostView use moveGhostView")
            val parent = element.parent as? ViewGroup
            val toPage = (stageRV.getChildViewHolder(toSnapLayout) as SnapLayoutHolder).page
            val to = toPagedPosition(toLayoutPosition, toPage)
            val lp = element.layoutParams as SnapLayout.SnapLayoutParams  // todo: it can error

            // todo b01-p (patched) {call of wrong function. It should be directed to addToFolder()}
//            if (dataset[to] != null)
//                return

            // move/add view
            lp.position = toLayoutPosition
            parent?.removeView(element)
            toSnapLayout.addView(element)

            // change data
            if (fromPosition != -1) {
                println("move")
                dataset.move(fromPosition, to)
            } else {
                println("add")
                when (element) {
                    is AppView -> dataset.put(to, element.info!!)
                }
            }
        }

        fun removeView(v: View) {
            println("removeView ${selectedView.className()}")
            if (v is WidgetView)
                dataKeeper.widgetHost.deleteAppWidgetId(v.widget.appWidgetId.also { println("id=$it") })
            val parent = v.parent as? ViewGroup
            val pos = toPagedPosition(selectedHolder.snapPosition)
            dataset.remove(pos)
            parent?.removeView(v)
        }

        fun startAddWidget(widgetPreview: WidgetPreview, toLayoutPosition: Int) {
            println("startAddWidget")
            check(widgetPreview == selectedView)
            selectedHolder.snapPosition = toLayoutPosition
            (context as LauncherActivity).requestCreateWidget(widgetPreview.widgetInfo)
        }

        fun cancelDrag() {
            removeFromDragLayer(selectedView)
            if (fromPosition != -1 && selectedView?.parent == null)
                currentSnapLayout.addView(selectedView)
        }









        fun createPreviewFolder(appView: AppView) {
            cancelPreviewFolder()
            val folder = FolderView(context, appView.info!!)
            currentSnapLayout.removeView(appView)
            currentSnapLayout.addNewView(folder, (appView.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
            previewFolder = folder
        }

        fun addToFolder(folder: FolderView, appView: AppView) {
            val folderPos = getPagedPositionOfView(folder)
            folder.addApps(appView.info!!)
            previewFolder = null
            (appView.parent as? ViewGroup)?.removeView(appView)
            dataset.put(folderPos, folder.info, true)
        }

        fun cancelPreviewFolder() {
            previewFolder?.let {
                currentSnapLayout.removeView(it)
                val appView = AppView(context, it.apps[0])
                currentSnapLayout.addNewView(appView, (it.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
                it.clear()
                previewFolder = null
                lastHoveredView = appView
            }
        }

        fun setFolderAnchorView(folder: FolderView) {
            val p = Point()
            getLocationOfViewGlobal(folder, p)
            folderWindow.x = p.x.toFloat()
            folderWindow.y = p.y.toFloat()
            println(p)
        }

        fun openFolder(folder: FolderView) {
            isFolderOpen = true
            folderWindow.setContent(folder, getPagedPositionOfView(folder))
            folderWindow.visibility = View.VISIBLE
            setFolderAnchorView(folder)
        }

        fun closeFolder() {
            isFolderOpen = false
            folderWindow.visibility = View.GONE
        }

        fun isTouchOutsideFolder(touchPoint: Point): Boolean {
            if (!isFolderOpen)
                return true
            findChildrenUnder(touchPoint).forEach {
                if (it is FolderWindow)
                    return false
            }
            return true
        }

        fun select(v: View?, setSnapLayoutParams: SnapLayout.SnapLayoutParams? = null): View? {
            println("try to select ${v.className()}; ${v?.layoutParams}")
            if (v == null || !isElement(v)) {
                fromPosition = -1
                selectedHolder.view = null
                return null
            }
            if (setSnapLayoutParams != null)
                v.layoutParams = setSnapLayoutParams
            selectedHolder.view = v
            fromPosition = if (v.parent == currentSnapLayout) selectedHolder.snapPosition + currentPage*pageSize else -1
            when (v) {
                is AppView -> onAppSelected(v)
                is FolderView -> onFolderSelected(v)
                is AppWidgetHostView -> onWidgetSelected(v)
            }
            println("selected = ${v.className()}, fromPosition = $fromPosition")
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

        fun flipPageIfNeeded(touchPoint: Point) {
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

        fun flipPage(direction: Int) {
            stageVP.currentItem += direction
            cancelPreviewFolder()
            closeFolder()
            flipPageDone = true
        }

        fun getElementPage(element: View): Int {
            val fromSnapLayout = element.parent as? SnapLayout ?: throw LauncherException("element $element not a child of SnapLayout. parent= ${element.parent}")
            return (stageRV.getChildViewHolder(fromSnapLayout) as SnapLayoutHolder).page
//            val pos = (element.layoutParams as SnapLayout.SnapLayoutParams).position
//            return pos / pageSize
        }

        fun getPagedPositionOfView(v: View): Int {
            val pos = (v.layoutParams as SnapLayout.SnapLayoutParams).position
            return pos + getElementPage(v) * pageSize
        }

        fun getPositionOnSnapUnder(snapLayout: SnapLayout, touchPoint: Point): Int {
            toLocationInView(touchPoint, snapLayout, reusablePoint)
            val step = if (selectedView is WidgetView || selectedView is WidgetPreview) 1 else 2
            val offsetX = step/2f*snapLayout.snapStepX.toFloat()
            val offsetY = step/2f*snapLayout.snapStepY.toFloat()
            reusablePoint.set(reusablePoint.x + offsetX.toInt(), reusablePoint.y + offsetY.toInt())
            return snapLayout.snapToGrid(reusablePoint, step)
        }

        fun toPagedPosition(layoutPosition: Int, page: Int = currentPage): Int {
            return layoutPosition + page * pageSize
        }

        fun dispatchWithTransform(v: View, event: MotionEvent) {
            val transformedEvent = getTransformedEvent(event, v)
            v.onTouchEvent(transformedEvent)
            transformedEvent.recycle()
        }

        fun getTransformedEvent(event: MotionEvent, v: View): MotionEvent {
            reusablePoint.set(event.x.toInt(), event.y.toInt())
            toLocationInView(reusablePoint, v, reusablePoint)
            val e = MotionEvent.obtain(event)
            e.setLocation(reusablePoint.x.toFloat(), reusablePoint.y.toFloat())
            return e
        }

        fun receiveFromFolder(folderWindow: FolderWindow, event: MotionEvent) {
            if (folderWindow.selectedApp != null) {
                val lp = SnapLayout.SnapLayoutParams(-1, 2, 2, currentSnapLayout)
                select(folderWindow.selectedApp!!.info!!.createView(context), lp)
                folderWindow.removeSelectedApp()
                closeFolder()
            }
        }

        fun startDragInFolder(event: MotionEvent) {
            val transformedEvent = getTransformedEvent(event, folderWindow)
            transformedEvent.action = MotionEvent.ACTION_DOWN
            folderWindow.startDrag(transformedEvent)
            transformedEvent.recycle()
        }
    }

}