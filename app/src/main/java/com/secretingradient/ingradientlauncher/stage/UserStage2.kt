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
import com.secretingradient.ingradientlauncher.drag.DragContext
import com.secretingradient.ingradientlauncher.drag.DragTouchEvent
import com.secretingradient.ingradientlauncher.drag.Hoverable
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

    override val dragContext = object : DragContext() {
        override val contentView: ViewGroup
            get() = this@UserStage2

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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isTouchInFolder(ev.x.toInt(), ev.y.toInt())) {
            reusablePoint.set(ev.x.toInt(), ev.y.toInt())
            transformDownToChild(folderWindow, this@UserStage2, reusablePoint)
            dispatchTransformedEvent(folderWindow, ev, reusablePoint)
        } else {
            if (ev.action == MotionEvent.ACTION_DOWN)
                closeFolder()
        }
        if (gestureHelper.gesture == Gesture.TAP_UP) {
            endEditMode()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        folderWindow = findViewById(R.id.folder_window)
        folderWindow.initData(dataset, defaultAppSize)  // todo: appSize?
        stageVP = findViewById(R.id.stage_1_pager)
        stageVP.adapter = adapter
        clipChildren = false
        hideSensors()
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

    override fun onLongClick(downEvent: MotionEvent) {
        startEditMode()
        if (isTouchInFolder(downEvent.x.toInt(), downEvent.y.toInt()))
            startDragInFolder(downEvent)
    }

    fun startEditMode() {
        inEditMode = true
        launcherActivity.dragController.startDragRequest()
        disallowVScroll()
        stageVP.animate().scaleX(0.85f).scaleY(0.85f).start()
    }

    fun endEditMode() {
        inEditMode = false
        dragContext.isDragEnabled = false
        stageVP.animate().scaleX(1f).scaleY(1f).start()
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
        val offsets = Point()
        this.getLocationOnScreen(offsets.asArray())
        folderWindow.setContent(folder, folder.getPagedPosition())
        folderWindow.visibility = View.VISIBLE
        folderWindow.translationX = globalX - offsets.x
        folderWindow.translationY = globalY - offsets.y
    }

    fun closeFolder() {
        isFolderOpen = false
        folderWindow.visibility = View.GONE
    }

    fun isTouchInFolder(x: Int, y: Int): Boolean {
        if (!isFolderOpen)
            return false
        folderWindow.getHitRect(reusableRect)
//        if (isGlobal) {
//            val p = Point()
//            folderWindow.getLocationOnScreen(p.asArray())
//            reusableRect.offsetTo(p.x, p.y)
//        }
        return reusableRect.contains(x, y)
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