package com.secretingradient.ingradientlauncher.stage

import android.graphics.Point
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.DataKeeper
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.toPx
import kotlin.math.abs

class MainStage(launcherRootLayout: LauncherRootLayout) : BaseStage(launcherRootLayout), View.OnTouchListener {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = DataKeeper.mainStageAppsData
    override val stageLayoutId = R.layout.stage_0_main
    lateinit var recyclerView: MainStageRecycler
    val gListener = GestureListener()
    val gDetector = GestureDetector(context, gListener)
    var isTransferred = false
    var state = -1
    var needInsert = false
    var insertedPos = -1
    lateinit var testApp: AppView
    private val touchPoint = Point()
    private val startDragOnFlyRunnable = object : Runnable {
        var event: MotionEvent? = null
        override fun run() {
            event?.let {
                println("try to start drag with position $insertedPos")
                recyclerView.itemTouchHelper.startDrag(recyclerView.findViewHolderForLayoutPosition(insertedPos)!!)
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
        // todo uncomment
/*
        recyclerView.saveListener = object : MainStageRecycler.OnSaveDataListener {
            override fun onSaveData() {
                saveData()
            }
        }
*/
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                TODO scroll wallpaper (dx, dy)
            }
        })
        stageRootLayout.setOnTouchListener(this)

        testApp = recyclerView.createAppView(apps[2])
        stageRootLayout.addView(testApp)
        testApp.visibility = View.INVISIBLE
    }

    override fun transferEvent(event: MotionEvent, v: AppView) {
        goToStage(0)
        needInsert = true
        stageRootLayout.overlayView = testApp
        launcherRootLayout.dispatchToCurrent = true // we need this. but for now UserStage already set this
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (needInsert) {
            disallowScrollStage()
            needInsert = false
            tryInsertOnFly(event, testApp)
            startDragOnFly(event)
        }

        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            launcherRootLayout.dispatchToCurrent = false
            needInsert = false
            stageRootLayout.overlayView = null
            recyclerView.onTouchEvent(event)
        }

        return gDetector.onTouchEvent(event)
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        var recognized = false
        val slop = toPx(5)

        override fun onDown(e: MotionEvent?): Boolean {
            recyclerView.onTouchEvent(e)
            recognized = false
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val dx = abs(e1.x - e2.x)
            val dy = abs(e1.y - e2.y)
            if (!recognized && dx*dx + dy*dy > slop*slop && dx > dy) {
                disallowScrollStage()
                recognized = true
            }
            if (recognized) {
                recyclerView.onTouchEvent(e2)
            }
            return recognized
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            recyclerView.fling(-velocityX.toInt(), 0)
            return true
        }

    }

    private fun saveData() {
        DataKeeper.dumpMainStageApps(context)
    }

    private fun tryInsertOnFly(event: MotionEvent, appView: AppView) {
        if (event.action == MotionEvent.ACTION_MOVE) {
            touchPoint.set(event.x.toInt(), event.y.toInt())
            val hitTestResult = getHitView(touchPoint)
            if (hitTestResult == recyclerView) {
                stageRootLayout.overlayView = null
                insertedPos = recyclerView.insertViewAt(appView, event.x - recyclerView.left, event.y - recyclerView.top)
            }
        }
    }

    private fun startDragOnFly(event: MotionEvent) {
        startDragOnFlyRunnable.event = event
        stageRootLayout.handler.post(startDragOnFlyRunnable)
    }
}