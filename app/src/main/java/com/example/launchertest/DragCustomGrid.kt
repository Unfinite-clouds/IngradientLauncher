package com.example.launchertest

import android.graphics.Point
import android.graphics.PointF
import android.view.DragEvent
import android.view.View
import com.example.launchertest.launcher_skeleton.AppShortcut
import com.example.launchertest.launcher_skeleton.DummyCell
import com.example.launchertest.launcher_skeleton.LauncherScreenGrid
import kotlin.math.abs

class DragCustomGrid: View.OnDragListener  {
    companion object {
        private var touchStartPoint: PointF? = null
        private var dragSide = Point()
        private var dragCell: DummyCell? = null
        private var dropCell: DummyCell? = null
        private var dragShortcut: AppShortcut? = null
    }

    override fun onDrag(cell: View?, event: DragEvent): Boolean {
        // cell is the cell under finger
        if (cell !is DummyCell) return false

        when (event.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                if (dragCell == null) {
                    dragCell = event.localState as DummyCell
                    dragShortcut = dragCell?.shortcut
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
                var result = true
                if (dragShortcut != null && cell.canMoveBy(-dragSide.x, -dragSide.y)) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating - just for prevent blinking
                    dragCell?.reserveShortcut() // save to temp var
                    dragCell?.removeAllViews() // now we can remove shortcut cause we have reserved state
                    cell.doMoveBy(-dragSide.x, -dragSide.y)
                    dragCell?.moveReservedShortcutIntoCell(cell)
                } else
                    result = false

/*                if (dragCell != null) {
                    cell.shortcut?.visibility = View.VISIBLE
                    cell.shortcut?.icon?.clearColorFilter()
                    (cell.parent as LauncherScreenGrid).dragEnded()
                    dragCell = null
                }*/

                return result
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                // back to default state
                if (dragCell != null) {
                    dragShortcut?.visibility = View.VISIBLE
                    dragShortcut?.icon?.clearColorFilter()
                    (cell.parent as LauncherScreenGrid).dragEnded()
                    dragCell = null
                }
                cell.defaultState()
            }
        }
        return true
    }

}
