package com.secretingradient.ingradientlauncher.stage

import android.content.Context
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

class UserStage2(context: Context, attrs: AttributeSet?) : PagedStage2(context, attrs), GestureHelper.GestureHelperListener {
    companion object {
        private val FLIP_WIDTH = toPx(25)
        private val defaultAppSize = toPx(70)
    }

    override var columnCount = getPrefs(context).getInt(Preferences.USER_STAGE_COLUMN_COUNT, -1)
    override var rowCount = getPrefs(context).getInt(Preferences.USER_STAGE_ROW_COUNT, -1)
    override var pageCount = getPrefs(context).getInt(Preferences.USER_STAGE_PAGE_COUNT, -1)
    override val adapter = PagedAdapter()
    override lateinit var stageVP: ViewPager2
    override val dataset: Dataset<Data, Info> = dataKeeper.userStageDataset

    override val dragContext = object : DragContext() {
        override val contentView: ViewGroup
            get() = this@UserStage2

        override fun onDrag(event: DragTouchEvent) {
            if (gestureHelper.gesture == Gesture.TAP_UP && event.draggableView !is FolderViewDraggable)
                endEditMode()
        }



        override fun returnThisHoverable(v: Hoverable): Boolean {
            return v !is SnapLayoutHoverable
        }
    }

    lateinit var folderWindow: FolderWindow
    var isFolderOpen = false
    val resizeFrame = View.inflate(context, R.layout.resize_frame, null) as WidgetResizeFrame


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (gestureHelper.gesture == Gesture.TAP_UP)
            endEditMode()
        return super.dispatchTouchEvent(ev)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        folderWindow = findViewById(R.id.folder_window)
        stageVP = findViewById(R.id.stage_1_pager)
        stageVP.adapter = adapter
        clipChildren = false
        hideSensors()
        // stageVP.offscreenPageLimit = 2
    }

    override fun bindPage(holder: PageHolder, page: Int) {
        dataset.forEach {
            if (isPosInPage(it.key, page)) {
                holder.snapLayout.addView(it.value.createView(context, true) // avoid creating here
                    .apply { setSnapLayoutParams(it.key % pageSize, it.value.snapWidth, it.value.snapHeight) }) // bad way
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
    }

    fun startEditMode() {
        launcherActivity.dragController.startDragRequest()
        disallowVScroll()
        stageVP.animate().scaleX(0.85f).scaleY(0.85f).start()
    }

    fun endEditMode() {
        dragContext.isDragEnabled = false
        stageVP.animate().scaleX(1f).scaleY(1f).start()
    }

}