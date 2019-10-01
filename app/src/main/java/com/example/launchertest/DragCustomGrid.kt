package com.example.launchertest

import android.graphics.Point
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.DragEvent
import android.view.View
import com.example.launchertest.launcher_skeleton.AppShortcut
import com.example.launchertest.launcher_skeleton.DummyCell
import com.example.launchertest.launcher_skeleton.LauncherScreenGrid
import kotlin.math.abs

class DragCustomGrid: View.OnDragListener  {
    companion object {
        // can we have two drag events at one moment?
        private var touchStartPoint: PointF? = null
        private var dragSide = Point()
        private var dragCell: DummyCell? = null
        private var dragShortcut: AppShortcut? = null
        private var isEnded = false
        private var hasDrop = false
        private var shortcuts_ = mutableListOf<AppShortcut>()
        private var drawables_ = mutableListOf<BitmapDrawable>()
        private var drawables_app_info = mutableListOf<BitmapDrawable>()
        private var states_ = mutableListOf<Drawable.ConstantState>()
        private var states_app_info = mutableListOf<Drawable.ConstantState>()
    }

    override fun onDrag(cell: View?, event: DragEvent): Boolean {
        // cell is the cell under finger
        if (cell !is DummyCell) return false

        when (event.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                if (dragShortcut == null) {
                    // will be called only once per drag event
                    val state = (event.localState as Pair<DummyCell?, AppShortcut>)
                    dragCell = state.first
                    dragShortcut = state.second
                    dragCell?.removeAllViews()
                    isEnded = false
                    hasDrop = false
                }
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                cell.setBackgroundResource(R.drawable.bot_gradient)
                dragSide = Point(0, 0)
                touchStartPoint = null
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                // remember the origin of coordinate system is [left, top]
                val newDragSide: Point =
                    if (event.y > event.x)
                        if (event.y > cell.height - event.x) Point(0, 1) else Point(-1, 0)
                    else
                        if (event.y > cell.height - event.x) Point(1, 0) else Point(0, -1)

                if (dragSide != newDragSide) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                    dragSide = newDragSide
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 100f)
                }

                if (touchStartPoint == null)
                    touchStartPoint = PointF(event.x, event.y)

                if (abs(touchStartPoint!!.x - event.x) > AppShortcut.DISMISS_RADIUS || abs(touchStartPoint!!.y - event.y) > AppShortcut.DISMISS_RADIUS) {
                    dragShortcut?.menuHelper?.dismiss()
                }

                (cell.parent as LauncherScreenGrid).tryFlipPage(cell, event)
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                cell.defaultState()
            }

            DragEvent.ACTION_DROP -> {
                // cell is the cell to drop
                dragShortcut?.icon?.clearColorFilter()
                if (cell.canMoveBy(-dragSide.x, -dragSide.y)) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating - just for prevent blinking
                    cell.doMoveBy(-dragSide.x, -dragSide.y)
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
                        dragCell?.shortcut = dragShortcut
                    } else {
                        // do nothing to let this shortcut to stay null and then deleted
                    }
                }
                if (!isEnded) {
                    // will be called only once per drag event
                    isEnded = true
                    val grid = (cell.parent as LauncherScreenGrid)
                    grid.dragEnded()
                    grid.saveState()
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
