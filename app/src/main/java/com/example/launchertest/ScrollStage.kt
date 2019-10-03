package com.example.launchertest

import android.content.ClipData
import android.content.Context
import android.graphics.PointF
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.setPadding
import kotlin.math.abs

class ScrollStage(context: Context) : BaseStage(context), View.OnLongClickListener, View.OnDragListener {
    var apps = AppManager.mainScreenApps
    val scrollId = R.id.main_stage_scroll
    val app_container = R.id.main_stage_app_container
    override val stageLayoutId = R.layout.stage_0_main_screen
    var widthCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_WIDTH_CELL, -1)
    var heightCell = getPrefs(context).getInt(Preferences.MAIN_SCREEN_HEIGHT_CELL, -1)

    override fun inflateAndAttach(rootLayout: ViewGroup) {
        super.inflateAndAttach(rootLayout)
        val container = rootLayout.findViewById<LinearLayout>(app_container)
        apps.forEach {
            val appInfo = AppManager.getApp(it.value)
            if (appInfo != null)
                container.addView(AppShortcut(context, appInfo).apply {
                    setOnLongClickListener(this@ScrollStage)
                    setOnDragListener(this@ScrollStage)
                    layoutParams = LinearLayout.LayoutParams(widthCell,heightCell)
                    setPadding(0)
                })
        }
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppShortcut) {
            v.showPopupMenu()
            v.startDrag(ClipData.newPlainText("",""), v.createDragShadow(), v, 0)
        }
        return true
    }


    private var touchStartPoint: PointF? = null
    private var dragDirection = 0
    private var dragShortcut: AppShortcut? = null
    private var isEnded = false
    private var hasDrop = false

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        if (v !is AppShortcut)
            return false
        
        when (event?.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                if (dragShortcut == null) {
                    // will be called only once per drag event
                    dragShortcut = event.localState as AppShortcut?
                    isEnded = false
                    hasDrop = false
                }
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                dragDirection = 0
                touchStartPoint = null
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                val newDragSide: Int =
                    if (event.x > v.width / 2) 1 else -1

                if (dragDirection != newDragSide) {
                    cell.doTranslateBy(-dragDirection.x, -dragDirection.y, 0f) // back translating
                    dragDirection = newDragSide
                    cell.doTranslateBy(-dragDirection.x, -dragDirection.y, 100f)
                }

                if (touchStartPoint == null)
                    touchStartPoint = PointF(event.x, event.y)

                if (abs(touchStartPoint!!.x - event.x) > AppShortcut.DISMISS_RADIUS || abs(
                        touchStartPoint!!.y - event.y) > AppShortcut.DISMISS_RADIUS) {
                    dragShortcut?.menuHelper?.dismiss()
                }

//                cell.parentGrid.tryFlipPage(cell, event)
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                cell.doTranslateBy(-dragDirection.x, -dragDirection.y, 0f) // back translating
                cell.defaultState()
            }

            DragEvent.ACTION_DROP -> {
                // cell is the cell to drop
                dragShortcut?.icon?.clearColorFilter()
                if (cell.canMoveBy(-dragDirection.x, -dragDirection.y)) {
                    cell.doTranslateBy(-dragDirection.x, -dragDirection.y, 0f) // back translating - just for prevent blinking
                    cell.doMoveBy(-dragDirection.x, -dragDirection.y)
                    cell.shortcut = dragShortcut
                    hasDrop = true
                } else {
                    return false
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                if (!hasDrop) {
                    // drag has been canceled
                    if (dragShortcut?.goingToRemove == false) {
                        dragCell?.shortcut =
                            dragShortcut
                    } else {
                        // do nothing to let this shortcut to stay null and then deleted
                    }
                }
                if (!isEnded) {
                    // will be called only once per drag event
                    isEnded = true
                    cell.parentGrid.dragEnded()
                    cell.parentGrid.saveState()
                    dragShortcut?.icon?.clearColorFilter()
                    dragCell = null
                    dragShortcut = null
                }
                cell.defaultState()
            }
        }
        return true
    }
}