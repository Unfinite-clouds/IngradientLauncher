package com.secretingradient.ingradientlauncher.stage

import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.element.AppView

class MainStage(launcherRootLayout: LauncherRootLayout) : BaseStage(launcherRootLayout), View.OnTouchListener {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = DataKeeper.mainStageAppsData
    override val stageLayoutId = R.layout.stage_0_main
    lateinit var recyclerView: MainStageRecycler
    val gestureRecognizer = GestureRecognizer(context)
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
        gestureRecognizer.onScrollDirectionRecognized = { scrollDirection ->
            if (scrollDirection == GestureRecognizer.ScrollDirection.DIRECTION_X)
                disallowScrollStage()
        }
        test()
    }

    private fun test() {
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
        gestureRecognizer.onTouchEvent(event)

        if (needInsert) {
            disallowScrollStage()
            if (tryInsertOnFly(event, testApp)) {
                startDragOnFly(event)
                needInsert = false
            }
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            recyclerView.onTouchEvent(event)
        }

        when(gestureRecognizer.gesture) {
            Gesture.SCROLL_X -> recyclerView.onTouchEvent(event)
        }

        if (event.action == MotionEvent.ACTION_UP) {
            launcherRootLayout.dispatchToCurrent = false
            needInsert = false
            stageRootLayout.overlayView = null
            recyclerView.onTouchEvent(event) // make fling
        }

        return true
    }

    private fun saveData() {
        DataKeeper.dumpMainStageApps(context)
    }

    private fun tryInsertOnFly(event: MotionEvent, appView: AppView): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE) {
            touchPoint.set(event.x.toInt(), event.y.toInt())
            val hitTestResult = getHitView(touchPoint)
            if (hitTestResult == recyclerView) {
                stageRootLayout.overlayView = null
                insertedPos = recyclerView.insertViewAt(appView, event.x - recyclerView.left, event.y - recyclerView.top)
                return true
            }
        }
        return false
    }

    private fun startDragOnFly(event: MotionEvent) {
        startDragOnFlyRunnable.event = event
        stageRootLayout.handler.post(startDragOnFlyRunnable)
    }
}