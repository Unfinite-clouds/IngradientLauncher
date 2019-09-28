package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import android.widget.GridLayout
import androidx.core.view.iterator
import com.example.launchertest.LauncherException
import com.example.launchertest.MainActivity
import kotlin.math.ceil
import kotlin.math.floor

class LauncherScreenGrid : GridLayout {
    private val flipPageRunnable = object : Runnable {
        override fun run() {
            if (flipDirection != 0) {
                (context as MainActivity).stageCustomGrid.currentItem += flipDirection
            }
            isWaitingForFlip = false
        }
    }

    fun handleDrag(cell: DummyCell, event: DragEvent) {
        if (cell.relativePosition.x == columnCount - 1 && event.x + cell.left > right - 50) {
            flipDirection = 1
            // TODO: start suspending scroll animation
        }
        else if (cell.relativePosition.x == 0 && event.x < 50) {
            flipDirection = -1
            // TODO: start suspending scroll animation
        }
        else
            dragExited()

        if (flipDirection != 0 && !isWaitingForFlip) {
            println("Post...")
            isWaitingForFlip = true
            handler.postDelayed(flipPageRunnable, 1000)
        }
    }

    fun dragExited() {
        flipDirection = 0
        isWaitingForFlip = false
        handler.removeCallbacks(flipPageRunnable)
    }

    var page = -1
    val size: Int
        get() = rowCount*columnCount
    lateinit var positions: IntArray  // global cell positions within whole stage
    private var widthCell = -1
    private var heightCell = -1
    private val decimalPadding = Rect()
    val gridBounds: IntRange
        get() = size*page until size*(page+1)
    private var isWaitingForFlip = false
    private var flipDirection = 0

    init {
        clipChildren = false
    }

    constructor(context: Context, nrows: Int, ncols: Int, page: Int = -1) : super(context) {
        println("creating Grid for page $page")
        this.page = page
        setGridSize(nrows, ncols)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setGridSize(rowCount, columnCount)
    }

    public fun setGridSize(nrows: Int, ncols: Int, fill: Boolean = true) {
        removeAllViews()
        rowCount = nrows
        columnCount = ncols
        positions = IntArray(size)
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
        for (pos in gridBounds) {
            addView(DummyCell(context, page*size+pos, toRelativePosition(pos)))
            positions[pos] = childCount-1
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

    fun addViewTo(child: View, pos: Int) {
        val cell = getCellAt(pos) ?: throw LauncherException("can't add view $child to position $pos; the position is out of gridBounds $gridBounds")
        cell.addView(child)
    }

    fun getCellAt(pos: Int): DummyCell? {
        if (checkCellAt(pos)) {
            return getChildAt(positions[pos])
        }
        return null
    }

    fun getCellAt(relativePos: Point): DummyCell? {
        return getCellAt(toGlobalPosition(relativePos))
    }


    fun checkCellAt(pos: Int): Boolean {
        if (pos in gridBounds && pos in positions) {
            return true
        }
        return false
    }

    fun toGlobalPosition(relativePos: Point): Int {
        return if (orientation == HORIZONTAL)
            page*size + columnCount*relativePos.y + relativePos.x
        else
            page*size + rowCount*relativePos.x + relativePos.y
    }

    fun toRelativePosition(pos: Int) : Point {
        val pos_relative =  pos % size

        return if (orientation == HORIZONTAL)
            Point(pos_relative % columnCount, pos_relative / columnCount)
        else
            Point(pos_relative / rowCount, pos_relative % rowCount)
    }

    fun getPageFromPos(pos: Int) : Int {
        return pos / size
    }
}