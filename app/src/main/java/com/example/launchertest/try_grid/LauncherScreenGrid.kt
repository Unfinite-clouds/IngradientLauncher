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

    val positions = Array(rowCount) { IntArray(columnCount) }
    var dragSide = Point(0, 0)

    init {
        fillEmptyGrid()
    }

    private fun fillEmptyGrid() {
        for (i in 0 until rowCount) {
            for (j in 0 until columnCount) {
                addView(DummyCell(context).apply {
                    position = Point(i,j)
                    layoutParams = LayoutParams(spec(position.x), spec(position.y))
                    layoutParams.width = cellWidth
                    layoutParams.height = cellHeight
                    (layoutParams as LayoutParams).setMargins(margins)
                    (layoutParams as LayoutParams).setGravity(Gravity.CENTER)
                    setOnDragListener(this@LauncherScreenGrid)
                })
                positions[i][j] = childCount-1
            }
        }
    }


    override fun getChildAt(index: Int): DummyCell {
        return super.getChildAt(index) as DummyCell
    }

    fun addViewTo(child: View, x: Int, y: Int) {
        getCellAt(x,y).addView(child)
    }

    fun getCellAt(x: Int, y: Int): DummyCell {
        return getChildAt(positions[x][y])
    }

    override fun onDrag(view: View?, event: DragEvent?): Boolean {
        if (view !is DummyCell || event == null)
            return false

        val dummyCell = view as DummyCell
        val shortcut = event.localState as ImageView

//        println("view=${dummyCell?.javaClass?.simpleName} ${dummyCell.hashCode()}, event.action=${event.action} event.loacalState=${event.localState.javaClass.simpleName} ${event.localState.hashCode()}")

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                dummyCell.onDragStarted()
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                dummyCell.onDragEntered()
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                // remember that origin of coordinate system is [left, top]
                if (event.y > event.x) dragSide = if (event.y > cellHeight - event.x) Point(0,1) else Point(-1,0)
                else dragSide = if (event.y > cellHeight - event.x) Point(1,0) else Point(0,-1)
                }
            DragEvent.ACTION_DRAG_EXITED -> {
                dummyCell.onDragExited()
            }
            DragEvent.ACTION_DROP -> {
                var cell = dummyCell
                while (cell.childCount != 0) {
                    cell = getCellAt(dummyCell.position.x + dragSide.x, dummyCell.position.y + dragSide.y)
                }
                moveShortcut(shortcut, dummyCell as ViewGroup)
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                // back to default state
                dummyCell.onDragEnded()
                endDrag(shortcut)
            }
        }
        return true
    }

    private fun moveShortcut(shortcut: ImageView, newDummy: ViewGroup) {
        (shortcut.parent as ViewGroup).removeView(shortcut)
        newDummy.addView(shortcut)
    }

    private fun endDrag(shortcut: ImageView) {
        shortcut.clearColorFilter()
        shortcut.visibility = View.VISIBLE
    }

    private fun swapShortcuts() {
        //
    }
}