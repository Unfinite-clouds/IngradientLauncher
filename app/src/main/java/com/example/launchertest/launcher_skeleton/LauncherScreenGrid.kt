package com.example.launchertest.launcher_skeleton

import android.content.ClipData
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.DragEvent
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.view.iterator
import com.example.launchertest.R
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class LauncherScreenGrid : GridLayout, View.OnDragListener, MenuItem.OnMenuItemClickListener, View.OnLongClickListener {
    constructor(context: Context, nrows: Int, ncols: Int) : super(context) {
        setGridSize(nrows, ncols)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setGridSize(rowCount, columnCount)
    }

    // TODO: remove magic values
    private var cellMargins = 0
    private var cellPadding = 20
    private var widthCell = -1
    private var heightCell = -1
    private var iconSizeDesired = 0.75f

    lateinit var positions: Array<IntArray>
    private var dragSide = Point(0, 0)
    private var dragStartPoint: PointF? = null
    private val radius = 20
    private lateinit var menuHelper: MenuPopupHelper
    private val decimalPadding = Rect()

    init {
        clipChildren = false
    }

    fun setGridSize(nrows: Int, ncols: Int, fill: Boolean = true) {
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

            // if cells can't fill full size due to int division, we will add padding to Grid
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
                addView(DummyCell(context, x, y).apply {
                    setOnDragListener(this@LauncherScreenGrid)
                })
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

    override fun onDrag(cell: View?, event: DragEvent?): Boolean {
        if (cell !is DummyCell || event == null)
            return false

        val shortcut = event.localState as AppShortcut

        when (event.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                cell.onDragStarted()
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                cell.onDragEntered()
                dragSide = Point(0, 0)
                dragStartPoint = null

            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                val newDragSide: Point
                // remember that the origin of coordinate system is [left, top]
                if (event.y > event.x) newDragSide =
                    if (event.y > heightCell - event.x) Point(0, 1) else Point(-1, 0)
                else newDragSide =
                    if (event.y > heightCell - event.x) Point(1, 0) else Point(0, -1)

                if (dragSide != newDragSide) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                    dragSide = newDragSide
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 100f)
                }

                if (dragStartPoint == null) {
                    dragStartPoint = PointF(event.x, event.y)
                }
                if (abs(dragStartPoint!!.x - event.x) > radius || abs(dragStartPoint!!.y - event.y) > radius) {
                    menuHelper.dismiss()
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

    private fun endDrag(shortcut: AppShortcut) {
        shortcut.icon?.clearColorFilter()
        shortcut.visibility = View.VISIBLE
    }

    override fun onLongClick(view: View?): Boolean {
        createPopupMenu(view!!)
        startDrag(view!! as AppShortcut)
        return true
    }

    private fun startDrag(shortcut: AppShortcut) {
        shortcut.visibility = View.INVISIBLE
        shortcut.icon?.setColorFilter(Color.rgb(181, 232, 255), PorterDuff.Mode.MULTIPLY)

        val cell = (shortcut.parent as DummyCell)
        cell.isReserved = true

        val data = ClipData.newPlainText("", "")
        val shadowBuilder = View.DragShadowBuilder(shortcut)
        shortcut.startDrag(data, shadowBuilder, shortcut, 0)
    }

    fun createPopupMenu(view: View) {
        val builder = MenuBuilder(view.context)
        val inflater = MenuInflater(view.context)
        inflater.inflate(R.menu.shortcut_popup_menu, builder)
        for (item in builder.iterator()) {
            item.setOnMenuItemClickListener(this)
        }
        menuHelper = MenuPopupHelper(view.context, builder, view)
        menuHelper.setForceShowIcon(true)
        menuHelper.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        println(item?.title)
        return true
    }
}