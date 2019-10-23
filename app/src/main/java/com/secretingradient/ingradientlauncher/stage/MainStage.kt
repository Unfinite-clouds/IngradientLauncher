package com.secretingradient.ingradientlauncher.stage

import android.content.ClipData
import android.content.Context
import android.graphics.PointF
import android.view.DragEvent
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.DataKeeper
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.toPx
import kotlin.math.abs

class MainStage(context: Context) : BaseStage(context), View.OnLongClickListener, View.OnDragListener, View.OnTouchListener {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = DataKeeper.mainStageAppsData
    override val stageLayoutId = R.layout.stage_0_main_screen
    lateinit var recyclerView: MainStageRecycler
    val gListener = GestureListener()
    val gDetector = GestureDetector(context, gListener)


    override fun inflateAndAttach(stageRoot: StageRoot) {
        super.inflateAndAttach(stageRoot)
        recyclerView = stageRoot.findViewById(R.id.stage_0_recycler)
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
        stageRoot.setOnTouchListener(this)
    }

    override fun adaptApp(app: AppView) {
//        app.setOnLongClickListener(this@MainStage)
    }

    private fun saveData() {
        DataKeeper.dumpMainStageApps(context)
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> stageRoot.requestDisallowInterceptTouchEvent(false)
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
                    recognized = true
                    stageRoot.requestDisallowInterceptTouchEvent(true)
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

    private var dragApp: AppView? = null
    private var isFirstDrag = true

    override fun onLongClick(v: View?): Boolean {
/*        if (v is AppView) {
            v.showMenu()
            startDrag(v)
        }*/
        return false
    }

    override fun startDrag(v: View) {
        if (v is AppView) {
            v.startDrag(ClipData.newPlainText("", ""), v.createDragShadow(), DragState(v, this), 0)
        }
    }

    override fun onFocus(event: DragEvent) {
/*        isFirstDrag = true
        dragApp = getParcelApp(event)

        if (isMyEvent(event)) {
            recyclerView.startDragWith(dragApp!!.parent as DummyCell)
        } else {
            recyclerView.startDrag()
        }*/
    }

    override fun onFocusLost(event: DragEvent) {
    }

    override fun onDragEnded(event: DragEvent) {
/*        recyclerView.stopDragScroll()
        saveData()
        dragApp = null*/
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        super.onDrag(v, event)

/*        when (event?.action) {

            DragEvent.ACTION_DRAG_STARTED -> {}

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (v is DummyCell) {
                    recyclerView.moveOrInsertDragged(v, dragApp!!.appInfo)
                } else if (v is FrameLayout) {
                    println("remove")
                    recyclerView.removeDragged()
                }
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                if (isFirstDrag) isFirstDrag = false else dragApp?.dismissMenu()

                if (v is DummyCell) {
//                    recyclerView.checkAndScroll(toParentCoords(v, event))
                } else if (v is FrameLayout) {
                    // v is root - FrameLayout
                    when {
*//*                        event.x > v.width - SCROLL_ZONE -> recyclerView.startDragScroll(+1)
                        event.x < SCROLL_ZONE -> recyclerView.startDragScroll(-1)*//*
//                        event.y > v.height - FLIP_ZONE -> flipToStage(1, event)
                        else -> recyclerView.stopDragScroll()
                    }
                }
            }

            DragEvent.ACTION_DRAG_EXITED -> {}

            DragEvent.ACTION_DROP -> {}

            DragEvent.ACTION_DRAG_ENDED -> {}
        }*/
        return true
    }

    private fun toParentCoords(v: View, event: DragEvent): PointF {
        return PointF(v.left + event.x, v.top + event.y)
    }
}