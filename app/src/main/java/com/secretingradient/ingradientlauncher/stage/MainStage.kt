package com.secretingradient.ingradientlauncher.stage

import android.graphics.Point
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.element.AppView

class MainStage(launcherRootLayout: LauncherRootLayout) : BaseStage(launcherRootLayout), View.OnTouchListener {

    override val stageLayoutId = R.layout.stage_0_main
    private lateinit var recyclerView: MainStageRecycler
    private var isTransferring = false
    private var isDragIntercepted = false
    private val touchPoint = Point()
    private val dataset = dataKeeper.mainStageDataset

    private val interceptDragRunnable = object : Runnable {
        var insertedPos = -1
        var event: MotionEvent? = null
        override fun run() {
            event?.let {
                val holder = recyclerView.findViewHolderForLayoutPosition(insertedPos)
                if (holder == null) {
                    recyclerView.scrollToPosition(insertedPos)
                    stageRootLayout.handler.post(this)
                    return
                }
                recyclerView.itemTouchHelper.startDrag(holder)
                val tmpAction = it.action
                val tmpPoint = PointF(it.x, it.y)
                val v = holder.itemView
                it.action = MotionEvent.ACTION_DOWN
                it.setLocation(v.left + v.height/2f, v.top + v.width/2f)
                recyclerView.onInterceptTouchEvent(it)
                it.setLocation(tmpPoint.x, tmpPoint.y)
                it.action = tmpAction
            }
        }
    }

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        recyclerView = stageRootLayout.findViewById(R.id.stage_0_recycler)
        recyclerView.init(dataset)
        recyclerView.addOnScrollListener(scroller)
        stageRootLayout.setOnTouchListener(this)
    }

    override fun onStageAttachedToWindow() {
        if (isTransferring) {
            disallowVScroll()
            isDragIntercepted = false
        }
    }

    override fun onStageSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        scroller.maxScroll = recyclerView.computeHorizontalScrollRange() - recyclerView.width
    }

    override fun receiveTransferEvent(obj: Any?) {
        if (obj !is AppView)
            return
        isTransferring = true
        obj.width = recyclerView.widthCell
        obj.height = recyclerView.heightCell
        stageRootLayout.overlayView = obj
        launcherRootLayout.dispatchToCurrentStage = true
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (!isTransferring) {
            recyclerView.onTouchEvent(event)
        } else if (!isDragIntercepted) {
            isDragIntercepted = tryInterceptDrag(event)
        } else {
            event.offsetLocation(-recyclerView.left.toFloat(), -recyclerView.top.toFloat())
            recyclerView.onTouchEvent(event)
            event.offsetLocation(recyclerView.left.toFloat(), recyclerView.top.toFloat())
        }

        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
            resetEventState()

        return true
    }

    private fun tryInterceptDrag(event: MotionEvent): Boolean {
        touchPoint.set(event.x.toInt(), event.y.toInt())
        val hoveredView = findInnerViewUnder(touchPoint)
        // hoveredView will one of appViews in RecyclerView
        if (hoveredView is AppView) {
            interceptDragRunnable.insertedPos = recyclerView.insertViewUnder(stageRootLayout.overlayView as AppView, event.x - recyclerView.left, event.y - recyclerView.top)
            interceptDragRunnable.event = event
            stageRootLayout.handler.post(interceptDragRunnable)
            stageRootLayout.overlayView = null
            return true
        } else if (hoveredView is MainStageRecycler)
            TODO("NOT IMPLEMENTED")

        return false
    }

    private fun resetEventState() {
        launcherRootLayout.dispatchToCurrentStage = false
        isTransferring = false
        isDragIntercepted = false
        stageRootLayout.overlayView = null
        stageRootLayout.handler.removeCallbacks(interceptDragRunnable)
    }
}