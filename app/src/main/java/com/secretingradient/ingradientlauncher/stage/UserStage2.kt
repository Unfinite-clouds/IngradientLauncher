package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.data.Data
import com.secretingradient.ingradientlauncher.data.Dataset
import com.secretingradient.ingradientlauncher.data.Info
import com.secretingradient.ingradientlauncher.drag.*
import com.secretingradient.ingradientlauncher.element.FolderViewDraggable
import com.secretingradient.ingradientlauncher.sensor.BaseSensor

class UserStage2(context: Context, attrs: AttributeSet?) : PagedStage2(context, attrs), GestureHelper.GestureHelperListener, View.OnTouchListener {
    companion object {
        private val FLIP_WIDTH = toPx(25)
        private val defaultAppSize = toPx(70)

        private val tmpPoint = Point()
        private val reusablePoint = Point()
        private val reusablePointF = PointF()
        private val reusableRect = Rect()
    }

    override var columnCount = getPrefs(context).getInt(Preferences.USER_STAGE_COLUMN_COUNT, -1)
    override var rowCount = getPrefs(context).getInt(Preferences.USER_STAGE_ROW_COUNT, -1)
    override var pageCount = getPrefs(context).getInt(Preferences.USER_STAGE_PAGE_COUNT, -1)
    override val adapter = PagedAdapter()
    override lateinit var stageVP: ViewPager2
    override val dataset: Dataset<Data, Info> = dataKeeper.userStageDataset
    lateinit var folderWindow: FolderWindow
    var isFolderOpen = false
    val resizeFrame = View.inflate(context, R.layout.resize_frame, null) as WidgetResizeFrame
    var inEditMode = false
    var isDragStartedInFolder = false

    override val dragContext = object : DragContext() {
        override val contentView: ViewGroup
            get() = this@UserStage2
        override val dataset: Dataset<Data, Info>
            get() = this@UserStage2.dataset

        override fun returnThisHoverable(v: Hoverable): Boolean {
            return v !is SnapLayoutHoverable
        }

        override fun onDrag(event: DragTouchEvent) {
            if (gestureHelper.gesture == Gesture.TAP_UP) {
                openFolder(event)
            }

            reusablePoint.set(event.touchPointRaw.x.toInt(), event.touchPointRaw.y.toInt())
            transformToContentView(reusablePoint)

            if (isTouchInFolder(reusablePoint.x, reusablePoint.y)) {
                // touch in FolderWindow

            } else {
                if (event.motionEvent.action == MotionEvent.ACTION_DOWN)
                    closeFolder()
            }
        }

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        folderWindow = findViewById(R.id.folder_window)
        folderWindow.initData(dataset, defaultAppSize)  // todo: appSize?
        stageVP = findViewById(R.id.stage_1_pager)
        stageVP.adapter = adapter
        clipChildren = false
        hideSensors()
        val rightSensor = findViewById<BaseSensor>(R.id.right_sensor)
        rightSensor.sensorListener = object : HoverableImpl() {
            override fun onHoverIn(event: DragTouchEvent) {
                stageVP.currentItem += 1
                closeFolder()
            }
        }
        val leftSensor = findViewById<BaseSensor>(R.id.left_sensor)
        leftSensor.sensorListener = object : HoverableImpl() {
            override fun onHoverIn(event: DragTouchEvent) {
                stageVP.currentItem -= 1
                closeFolder()
            }
        }
        // stageVP.offscreenPageLimit = 2
    }

    override fun bindPage(holder: PageHolder, page: Int) {
        dataset.forEach {
            if (isPosInPage(it.key, page)) {
                val newElement = it.value.createView(context, true) // avoid creating here
                    .apply {
                        setSnapLayoutParams(it.key % pageSize, it.value.snapWidth, it.value.snapHeight)
                    } // bad way
                holder.snapLayout.addView(newElement)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // dispatching to FolderWindow when needed
        // tap out of FolderWindow = endEditMode()

        if (ev.action == MotionEvent.ACTION_DOWN || ev.action == MotionEvent.ACTION_UP) {
            // reset
            isDragStartedInFolder = false
        }

        if (isTouchInFolder(ev.x.toInt(), ev.y.toInt())) {
            if (ev.action == MotionEvent.ACTION_DOWN ) {
                isDragStartedInFolder = true
            }
//            reusablePoint.set(ev.x.toInt(), ev.y.toInt())
//            transformDownToChild(folderWindow, this@UserStage2, reusablePoint)
//            dispatchTransformedEvent(folderWindow, ev, reusablePoint)
        } else {
            if (gestureHelper.gesture == Gesture.TAP_UP) {
                if (!isFolderOpen)
                    endEditMode()
                else
                    closeFolder()
            }

            if (gestureHelper.gesture == Gesture.SCROLL_X_MOVE || gestureHelper.gesture == Gesture.SCROLL_Y_MOVE)
                closeFolder()

            if (isDragStartedInFolder && folderWindow.selectedApp != null) {
                // we have to intercept the drag with a new app
                closeFolder()
                isDragStartedInFolder = false
                val app = folderWindow.selectedApp!!.info!!.createView(context, true)
                app.setSnapLayoutParams(-1)
                app.layoutParams.width = defaultAppSize  // todo: deal with size
                app.layoutParams.height = defaultAppSize
                folderWindow.removeSelectedApp()
                dragContext.pendingActions.add(0) {dataset.put(folderWindow.folderPosInDataset, folderWindow.folderInfo, true, false)}
                dragContext.dragController.startDragRequest(app as Draggable)
            }
        }

        super.dispatchTouchEvent(ev)
        return true
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (v !is FolderViewDraggable)
            return v?.onTouchEvent(event) ?: false

        if (gestureHelper.gesture == Gesture.TAP_UP) {
            val p = Point()
            v.getLocationOnScreen(p.asArray())
            openFolder(v, p.x.toFloat(), p.y.toFloat())
        }
        return v.onTouchEvent(event)
    }

    override fun onLongClick(downEvent: MotionEvent) {
        startEditMode()
        if (isTouchInFolder(downEvent.x.toInt(), downEvent.y.toInt()))
            startDragInFolder(downEvent)
    }

    fun showSensors(opacity: Int) {
        children.forEach {
            if (it is BaseSensor) {
                it.visibility = View.VISIBLE
                it.drawable.alpha = opacity
            }
        }
    }

    fun hideSensors() {
        children.forEach {
            if (it is BaseSensor) {
                it.visibility = View.INVISIBLE
            }
        }
    }

    fun startEditMode() {
        if (!inEditMode) {
            inEditMode = true
            closeFolder()
            launcherActivity.dragController.startDragRequest()
            disallowVScroll()
            stageVP.animate().scaleX(0.85f).scaleY(0.85f).start()
            showSensors(255)
        }
    }

    fun endEditMode() {
        inEditMode = false
        dragContext.isDragEnabled = false
        stageVP.animate().scaleX(1f).scaleY(1f).start()
        hideSensors()
    }

    private fun openFolder(event: DragTouchEvent) {
        val draggable = event.draggableView
        if (draggable is FolderViewDraggable) {
            event.getMatrixDTranslation(reusablePointF)
            openFolder(draggable, reusablePointF.x, reusablePointF.y)
        }
    }

    fun openFolder(folder: FolderViewDraggable, globalX: Float, globalY: Float) {
        isFolderOpen = true
        folderWindow.visibility = View.VISIBLE
        val offsets = Point()
        this.getLocationOnScreen(offsets.asArray())
        folderWindow.setContent(folder, folder.getPagedPosition())
        val clp = (folderWindow.layoutParams as LayoutParams)
        clp.leftMargin = (globalX - offsets.x).toInt()
        clp.topMargin = (globalY - offsets.y).toInt()
        folderWindow.requestLayout()
    }

    fun closeFolder() {
        isFolderOpen = false
        folderWindow.visibility = View.GONE
    }

    fun isTouchInFolder(x: Int, y: Int): Boolean {
        if (!isFolderOpen)
            return false
        folderWindow.getHitRect(reusableRect)
        return reusableRect.contains(x, y)
    }

    fun transformToContentView(pointGlobal: Point) {
        if (pointGlobal === tmpPoint) throw LauncherException("use another point object, tmpPoint is reserved")
        getLocationOnScreen(tmpPoint.asArray())
        pointGlobal.offset(-tmpPoint.x, -tmpPoint.y)
    }

    fun transformDownToChild(toChild: View, fromParent: View, pointInParent: Point) {
        var v = toChild as View?
        while (v != fromParent && v != null) {
            // scale?
            pointInParent.offset(-v.x.toInt(), -v.y.toInt())
            v = v.parent as View?
        }
        if (v == null)
            throw LauncherException("view $fromParent must be a subchild of $toChild")
    }

    fun dispatchTransformedEvent(v: View, event: MotionEvent, point: Point) {
        val x = event.x
        val y = event.y
        event.setLocation(point.x.toFloat(), point.y.toFloat())
        v.onTouchEvent(event)
        event.setLocation(x, y)
    }

    fun startDragInFolder(downEvent: MotionEvent) {
        val x = downEvent.x
        val y = downEvent.y
        reusablePoint.set(x.toInt(), y.toInt())
        transformDownToChild(folderWindow, this, reusablePoint)
        downEvent.setLocation(reusablePoint.x.toFloat(), reusablePoint.y.toFloat())
        folderWindow.startDrag(downEvent)
        downEvent.setLocation(x, y)
    }
}