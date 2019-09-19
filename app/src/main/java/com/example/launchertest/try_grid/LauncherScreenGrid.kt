package com.example.launchertest.try_grid

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import androidx.core.view.setMargins

class LauncherScreenGrid : GridLayout, View.OnDragListener{
    constructor(context: Context, nrows: Int, ncols: Int) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    // TODO: remove magic values
    private var cellWidth = 144
    private var cellHeight = 144
    private var margins = 20

    val positions = Array(rowCount) { IntArray(columnCount) }

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

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        return true
    }
}