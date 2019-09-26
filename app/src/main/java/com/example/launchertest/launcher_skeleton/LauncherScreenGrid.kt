package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.GridLayout
import androidx.core.view.iterator
import kotlin.math.ceil
import kotlin.math.floor

class LauncherScreenGrid : GridLayout {
    // TODO: remove magic values
    private var widthCell = -1
    private var heightCell = -1

    lateinit var positions: Array<IntArray>
    private val decimalPadding = Rect()

    init {
        clipChildren = false
    }

    constructor(context: Context, nrows: Int, ncols: Int) : super(context) {
        setGridSize(nrows, ncols)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setGridSize(rowCount, columnCount)
    }

    private fun setGridSize(nrows: Int, ncols: Int, fill: Boolean = true) {
        removeAllViews()
        rowCount = nrows
        columnCount = ncols
        positions = Array(columnCount) { IntArray(rowCount) }
        if (fill) fillEmptyGrid()
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val myWidth = MeasureSpec.getSize(widthSpec)
        val myHeight = MeasureSpec.getSize(heightSpec)

        if (myWidth != measuredWidth || myHeight != measuredHeight) {
            widthCell = myWidth/columnCount
            heightCell = myHeight/rowCount

            this.iterator().forEach {
                it.layoutParams.width = widthCell
                it.layoutParams.height = heightCell
            }

            // if cells can't fill full size due to int division, we will add little padding to Grid
            val decimalWidth = myWidth - widthCell*columnCount
            val decimalHeight = myHeight - heightCell*rowCount

            decimalPadding.set(floor(decimalWidth.toFloat()/2f).toInt(),
                floor(decimalHeight.toFloat()/2f).toInt(),
                ceil(decimalWidth.toFloat()/2f).toInt(),
                ceil(decimalHeight.toFloat()/2f).toInt())
        }

        val widthChildSpec = MeasureSpec.makeMeasureSpec(widthCell, MeasureSpec.EXACTLY)
        val heightChildSpec = MeasureSpec.makeMeasureSpec(heightCell, MeasureSpec.EXACTLY)
        measureChildren(widthChildSpec, heightChildSpec)
        setMeasuredDimension(myWidth, myHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        setPadding(paddingLeft + decimalPadding.left,
            paddingTop + decimalPadding.top,
            paddingRight + decimalPadding.right,
            paddingBottom + decimalPadding.bottom)
        super.onLayout(changed, left, top, right, bottom)
    }

    private fun fillEmptyGrid() {
        for (y in 0 until rowCount) {
            for (x in 0 until columnCount) {
                addView(DummyCell(context, pointToPos(x,y), x, y))
                positions[x][y] = childCount-1
            }
        }
    }

    fun clearGrid() {
        for (cell in iterator()) {
            (cell as DummyCell).removeAllViews()
        }
    }

    override fun getChildAt(index: Int): DummyCell {
        return super.getChildAt(index) as DummyCell
    }

    fun addViewTo(child: View, x: Int, y: Int) {
        getCellAt(x,y)!!.addView(child)
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

    fun pointToPos(x: Int, y: Int, page: Int = 0): Int {
        return if (orientation == HORIZONTAL) page*columnCount*rowCount + columnCount*y + x else page*columnCount*rowCount + rowCount*x + y
    }

    fun pointToPos(pos: Point, page: Int = 0): Int {
        return pointToPos(pos.x, pos.y, page)
    }

    fun posToPoint(pos: Int) : Point {
        val pos_ =  pos % (columnCount*rowCount)
        return if (orientation == HORIZONTAL) Point(pos_ % columnCount, pos_ / rowCount) else Point(pos_ / columnCount, pos_ % rowCount)
    }

    fun getPageFromPos(pos: Int) : Int {
        return pos / (columnCount*rowCount)
    }
}