package com.secretingradient.ingradientlauncher.stage

import android.graphics.PointF
import android.view.DragEvent
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

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        recyclerView = stageRootLayout.findViewById(R.id.stage_0_recycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.apps = apps
        recyclerView.saveListener = object : MainStageRecycler.OnSaveDataListener {
            override fun onSaveData() {
                saveData()
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                TODO scroll wallpaper (dx, dy)
            }
        })
        stageRootLayout.setOnTouchListener(this)
    }

    override fun transferEvent(event: MotionEvent, v: AppView) {
        goToStage(0)
        apps.add(0, v.appInfo.id)
        isTransferred = true
        recyclerView.adapter?.notifyItemInserted(0)
//        launcherRootLayout.dispatchToCurrent = true // we need this. but for now UserStage will set this
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        println("MainOnTouch")
        if (isTransferred) {
            isTransferred = false
            state = 0
        }
        if (state == 0) {
            val transferredVH = recyclerView.findViewHolderForAdapterPosition(0)
            if (transferredVH != null) {
                println("found")
                state = -1
                recyclerView.itemTouchHelper.startDrag(transferredVH)
            } else {
            }
        }

        if (event.action == MotionEvent.ACTION_UP)
            launcherRootLayout.dispatchToCurrent = false

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
                    recognized = true
                    stageRootLayout.requestDisallowInterceptTouchEvent(true)
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

    private fun toParentCoords(v: View, event: DragEvent): PointF {
        return PointF(v.left + event.x, v.top + event.y)
    }
}