package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import com.example.launchertest.AppManager
import com.example.launchertest.LauncherException
import com.example.launchertest.R
import com.example.launchertest.launcher_skeleton.AppShortcut.Companion.DISMISS_RADIUS
import kotlin.math.abs


class DummyCell : LinearLayout, View.OnDragListener {

    private var reservedShortcut: AppShortcut? = null // needed only for swap apps
    lateinit var relativePosition: Point // the position within one ScreenGrid (not considering page number)
    var position: Int = -1
    private val bgcolor = Color.argb(40,0,0,0)
    private var dragSide = Point()
    val shortcut: AppShortcut?
        get() = getChildAt(0) as? AppShortcut ?: reservedShortcut
    private var touchStartPoint: PointF? = null

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(bgcolor)
        setOnDragListener(this)
    }

    constructor(context: Context, position: Int, relativePosition: Point) : super(context) {
        this.position = position
        this.relativePosition = relativePosition
        layoutParams = GridLayout.LayoutParams(GridLayout.spec(relativePosition.y),GridLayout.spec(relativePosition.x))
        layoutParams.width = 0
        layoutParams.height = 0
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    override fun onViewAdded(child: View?) {
        if (childCount > 1) {
            throw LauncherException("$this can only have 1 child")
        }
        child?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        super.onViewAdded(child)
    }

    override fun onDrag(cell: View?, event: DragEvent): Boolean {
        // cell is the cell under finger
        if (cell !is DummyCell) return false

//        val dragCell = event.localState as DummyCell
//        val dragShortcut = dragCell.shortcut

        when (event.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                (event.localState as DummyCell).reserveShortcut()
                setBackgroundResource(R.drawable.bot_gradient)
                dragSide = Point(0, 0)
                touchStartPoint = null
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                // remember the origin of coordinate system is [left, top]
                val newDragSide: Point =
                    if (event.y > event.x)
                        if (event.y > height - event.x) Point(0, 1) else Point(-1, 0)
                    else
                        if (event.y > height - event.x) Point(1, 0) else Point(0, -1)

                if (dragSide != newDragSide) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                    dragSide = newDragSide
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 100f)
                }

                if (touchStartPoint == null)
                    touchStartPoint = PointF(event.x, event.y)
                if (abs(touchStartPoint!!.x - event.x) > DISMISS_RADIUS || abs(touchStartPoint!!.y - event.y) > DISMISS_RADIUS) {
                    cell.shortcut?.menuHelper?.dismiss()
                }


                (cell.parent as LauncherScreenGrid).tryFlipPage(cell, event)
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                setBackgroundColor(bgcolor)
            }

            DragEvent.ACTION_DROP -> {
                // cell is the cell to drop

                val dragCell = event.localState as DummyCell
                val dragShortcut = dragCell.shortcut
                if (dragShortcut != null && cell.canMoveBy(-dragSide.x, -dragSide.y)) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating - just for prevent blinking
                    cell.doMoveBy(-dragSide.x, -dragSide.y)
                    dragCell.moveShortcutIntoCell(cell)
                } else
                    return false
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                // back to default state
                removeReserve()
                (cell.parent as LauncherScreenGrid).dragEnded()
                cell.shortcut?.visibility = View.VISIBLE
                cell.shortcut?.icon?.clearColorFilter()
                cell.shortcut?.translationX = 0f
                cell.shortcut?.translationY = 0f
                cell.setBackgroundColor(bgcolor)
            }
        }
        return true
    }

    private fun reserveShortcut() {
        reservedShortcut = shortcut
        removeAllViews()
    }

    private fun removeReserve() {
        if (reservedShortcut != null) {
            addView(reservedShortcut)
            reservedShortcut = null
        }
    }

    private fun moveShortcutIntoCell(newCell: DummyCell) {
        val shortcutTemp = reservedShortcut ?: shortcut
        if (shortcutTemp != null) {
            if (reservedShortcut == null)
                this.removeAllViews()
            newCell.addView(shortcutTemp)
            AppManager.applyCustomGridChanges(context, shortcutTemp.appInfo.id, newCell.position)
        }
        reservedShortcut = null
    }

    fun removeShortcut() {
        if (reservedShortcut != null) {
            AppManager.applyCustomGridChanges(context, reservedShortcut!!.appInfo.id, -1)
            reservedShortcut = null
        }
    }

    private fun doRecursionPass(directionX: Int, directionY: Int, action: (thisCell: DummyCell, nextCell: DummyCell) -> Unit): Boolean {
        if (isEmptyCell()) {
            return true
        }
        if (directionX == 0 && directionY == 0) {
            action(this, this)
            return true
        }
        val next = Point(relativePosition.x + directionX, relativePosition.y + directionY)
        val nextCell: DummyCell? = (parent as LauncherScreenGrid).getCellAt(next)
        if (nextCell?.doRecursionPass(directionX, directionY, action) == true) {
            action(this, nextCell)
            return true
        }
        return false
    }

    fun canMoveBy(directionX: Int, directionY: Int): Boolean {
        return doRecursionPass(directionX, directionY) { thisCell, nextCell -> }
    }

    fun doMoveBy(directionX: Int, directionY: Int): Boolean {
        return doRecursionPass(directionX, directionY) { thisCell, nextCell ->
            thisCell.moveShortcutIntoCell(nextCell)
        }
    }

    fun doTranslateBy(directionX: Int, directionY: Int, value: Float): Boolean {
        return doRecursionPass(directionX, directionY) { thisCell, nextCell ->
            thisCell.shortcut?.translationX = value*directionX
            thisCell.shortcut?.translationY = value*directionY
        }
    }

    fun isEmptyCell(): Boolean {
        if (childCount == 0)
            return true
        return false
    }

    override fun toString(): String {
        return "\"${javaClass.simpleName}: $position empty=${isEmptyCell()} reserved=$reservedShortcut\""
    }
}