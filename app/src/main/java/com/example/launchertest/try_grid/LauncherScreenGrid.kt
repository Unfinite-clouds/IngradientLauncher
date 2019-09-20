package com.example.launchertest.try_grid

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.core.view.setMargins

class LauncherScreenGrid : GridLayout, View.OnDragListener{
    constructor(context: Context, nrows: Int, ncols: Int) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    // TODO: remove magic values
    private var cellWidth = 144
    private var cellHeight = 144
    private var margins = 20

    val positions = Array(columnCount) { IntArray(rowCount) }
    var dragSide = Point(0, 0)

    init {
        fillEmptyGrid()
    }

    private fun fillEmptyGrid() {
        for (y in 0 until rowCount) {
            for (x in 0 until columnCount) {
                addView(DummyCell(context).apply {
                    position = Point(x,y)
                    layoutParams = LayoutParams(spec(position.y), spec(position.x))
                    layoutParams.width = cellWidth
                    layoutParams.height = cellHeight
                    (layoutParams as LayoutParams).setMargins(margins)
                    (layoutParams as LayoutParams).setGravity(Gravity.CENTER)
                    setOnDragListener(this@LauncherScreenGrid)
                })
                positions[x][y] = childCount-1
            }
        }
    }


    override fun getChildAt(index: Int): DummyCell {
        return super.getChildAt(index) as DummyCell
    }

    fun addViewTo(child: View, x: Int, y: Int) {
        getCellAt(x,y)?.addView(child)
    }

    fun getCellAt(x: Int, y: Int): DummyCell? {
        if (checkCellAt(x, y)) {
            return getChildAt(positions[x][y])
        }
        return null
    }

    fun getCellAt(pos: Point): DummyCell? {
        return getCellAt(pos.x, pos.y)
    }

    fun checkCellAt(x: Int, y: Int): Boolean {
        if (x in 0 until columnCount && y in 0 until rowCount) {
            return true
        }
        return false
    }

    override fun onDrag(cell: View?, event: DragEvent?): Boolean {
        if (cell !is DummyCell || event == null)
            return false

        val shortcut = event.localState as ImageView

        when (event.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                cell.onDragStarted()
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                cell.onDragEntered()
                dragSide = Point(0, 0)

            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                val newDragSide: Point
                // remember that the origin of coordinate system is [left, top]
                if (event.y > event.x) newDragSide =
                    if (event.y > cellHeight - event.x) Point(0, 1) else Point(-1, 0)
                else newDragSide =
                    if (event.y > cellHeight - event.x) Point(1, 0) else Point(0, -1)

                if (dragSide != newDragSide) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                    dragSide = newDragSide
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 100f)
                }
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                cell.onDragExited()
            }

            DragEvent.ACTION_DROP -> {
                // cell is the cell to drop
                if (cell.canMoveBy(-dragSide.x, -dragSide.y)) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating - just for anti-blink
                    (shortcut.parent as DummyCell).removeView(shortcut)
                    cell.doMoveBy(-dragSide.x, -dragSide.y)
                    cell.addView(shortcut)
                } else return false
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                // back to default state
                cell.onDragEnded()
                endDrag(shortcut)
            }
        }
        return true
    }

    private fun moveShortcut(shortcut: ImageView, newCell: ViewGroup) {
        (shortcut.parent as ViewGroup).removeView(shortcut)
        newCell.addView(shortcut)
    }

    private fun endDrag(shortcut: ImageView) {
        shortcut.clearColorFilter()
        shortcut.visibility = View.VISIBLE
    }

    private fun swapShortcuts() {
        //
    }
}