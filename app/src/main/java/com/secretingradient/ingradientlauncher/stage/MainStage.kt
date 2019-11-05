package com.secretingradient.ingradientlauncher.stage

import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.DataKeeper
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.element.AppView

class MainStage(launcherRootLayout: LauncherRootLayout) : BaseStage(launcherRootLayout), View.OnTouchListener {

    var apps = DataKeeper.mainStageAppsData
    override val stageLayoutId = R.layout.stage_0_main
    private lateinit var recyclerView: MainStageRecycler
    private var isTransferring = false
    private val touchPoint = Point()
    private var isDragOnFly = false
    private val startDragOnFlyRunnable = object : Runnable {
        var insertedPos = -1
        var event: MotionEvent? = null
        override fun run() {
            event?.let {
                recyclerView.itemTouchHelper.startDrag(recyclerView.findViewHolderForLayoutPosition(insertedPos)!!)
                isDragOnFly = true
                val tmpAction = it.action
                it.action = MotionEvent.ACTION_DOWN
                recyclerView.onInterceptTouchEvent(it)
                it.action = tmpAction
            }
        }
    }

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        recyclerView = stageRootLayout.findViewById(R.id.stage_0_recycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.apps = apps
        recyclerView.saveListener = object : MainStageRecycler.OnSaveDataListener {
            override fun onSaveData() = saveData()
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                TODO scroll wallpaper (dx, dy)
            }
        })
        stageRootLayout.setOnTouchListener(this)
    }

    override fun receiveTransferredElement(element: AppView) {
        goToStage(0)
        isTransferring = true
        element.width = recyclerView.widthCell
        element.height = recyclerView.heightCell
        stageRootLayout.overlayView = element
        launcherRootLayout.dispatchToCurrentStage = true
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (!isTransferring) {
            recyclerView.onTouchEvent(event)

        } else if (handleInsertOnTransferring(event))
                isTransferring = false

        if (event.action == MotionEvent.ACTION_UP)
            resetEventState()

        return true
    }

    private fun handleInsertOnTransferring(event: MotionEvent): Boolean {
        disallowVScroll()
        touchPoint.set(event.x.toInt(), event.y.toInt())
        val hoveredView = findViewUnder(touchPoint)
        // hoveredView will one of appViews in RecyclerView
        if (hoveredView is AppView) {
            startDragOnFlyRunnable.insertedPos = recyclerView.insertViewUnder(stageRootLayout.overlayView as AppView, event.x - recyclerView.left, event.y - recyclerView.top)
            startDragOnFlyRunnable.event = event
            stageRootLayout.handler.post(startDragOnFlyRunnable)
            stageRootLayout.overlayView = null
            return true
        }

        return false
    }

    private fun resetEventState() {
        launcherRootLayout.dispatchToCurrentStage = false
        isTransferring = false
        isDragOnFly = false
        stageRootLayout.overlayView = null
    }

    private fun saveData() {
        DataKeeper.dumpMainStageApps(context)
    }

}