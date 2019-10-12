package com.secretingradient.ingradientlauncher.stage

import android.content.ClipData
import android.content.Context
import android.graphics.PointF
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import com.secretingradient.ingradientlauncher.AppManager
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.toPx

class MainStage(context: Context) : BaseStage(context), View.OnLongClickListener, View.OnDragListener {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = AppManager.mainScreenApps
    override val stageLayoutId = R.layout.stage_0_main_screen
    lateinit var recyclerView: MainStageRecycler


    override fun inflateAndAttach(rootLayout: ViewGroup) {
        super.inflateAndAttach(rootLayout)
        recyclerView = rootLayout.findViewById(R.id.stage_0_recycler)
        recyclerView.apps = apps
        recyclerView.saveListener = object : MainStageRecycler.OnSaveDataListener {
            override fun onSaveData() {
                saveData()
            }
        }
    }

    override fun adaptApp(app: AppView) {
//        app.setOnLongClickListener(this@MainStage)
    }

    private fun saveData() {
        AppManager.applyMainScreenChanges(context, apps)
        println("save data")
    }

    private var dragApp: AppView? = null
    private var isFirstDrag = true

    override fun onLongClick(v: View?): Boolean {
/*        if (v is AppView) {
            v.showMenu()
            startDrag(v)
        }*/
        return true
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