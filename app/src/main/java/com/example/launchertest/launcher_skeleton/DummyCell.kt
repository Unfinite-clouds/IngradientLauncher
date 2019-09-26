package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import com.example.launchertest.AppManager
import com.example.launchertest.LauncherException
import com.example.launchertest.R


class DummyCell : LinearLayout, View.OnDragListener {

    var isReserved: Boolean = false
    lateinit var oldPosition: Point
    var position: Int = -1
    private val bgcolor = Color.argb(40,0,0,0)
    private var dragSide = Point()
    val shortcut: AppShortcut?
        get() = getChildAt(0) as AppShortcut?

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(bgcolor)
        setOnDragListener(this)
    }

    constructor(context: Context?, position: Int, x: Int, y: Int) : super(context) {
        this.position = position
        oldPosition = Point(x,y)
        layoutParams = GridLayout.LayoutParams(GridLayout.spec(oldPosition.y),GridLayout.spec(oldPosition.x))
        layoutParams.width = 0
        layoutParams.height = 0
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

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

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        isReserved = false
    }

    override fun onDrag(cell: View?, event: DragEvent): Boolean {
        // cell is the cell under finger
        if (cell !is DummyCell) return false

        val dragShortcut = event.localState as AppShortcut

        when (event.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                setBackgroundResource(R.drawable.bot_gradient)
                dragSide = Point(0, 0)

            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                val newDragSide: Point
                // remember that the origin of coordinate system is [left, top]
                newDragSide =
                    if (event.y > event.x)
                        if (event.y > height - event.x) Point(0, 1) else Point(-1, 0)
                    else
                        if (event.y > height - event.x) Point(1, 0) else Point(0, -1)

                if (dragSide != newDragSide) {
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                    dragSide = newDragSide
                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 100f)
                }


//                if (abs(dragStartPoint!!.x - event.x) > DISMISS_RADIUS || abs(dragStartPoint!!.y - event.y) > DISMISS_RADIUS) {
//                    cell.shortcut.menuHelper?.dismiss()
//                }

//                if (cell.oldPosition.x == columnCount - 1 && dragSide == Point(1, 0)) {
//                    println("NEXT")
//                }
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating
                setBackgroundColor(bgcolor)
            }

            DragEvent.ACTION_DROP -> {
                // cell is the cell to drop
                if (cell.canMoveBy(-dragSide.x, -dragSide.y)) {
                    val oldCell =  (dragShortcut.parent as DummyCell)

                    // TODO: move this to add/removeView() or similar for auto-apply
                    AppManager.applyCustomGridChanges(context, dragShortcut.appInfo.id, cell.position)

                    cell.doTranslateBy(-dragSide.x, -dragSide.y, 0f) // back translating - just for prevent blinking
                    oldCell.removeView(dragShortcut)
                    cell.doMoveBy(-dragSide.x, -dragSide.y)
                    cell.addView(dragShortcut)
                } else
                    return false
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                // back to default state
                dragShortcut.visibility = View.VISIBLE
                dragShortcut.icon?.clearColorFilter()
                setBackgroundColor(bgcolor)
                isReserved = false
                this.shortcut?.translationX = 0f
                this.shortcut?.translationY = 0f
            }
        }
        return true
    }

    private fun doRecursionPass(directionX: Int, directionY: Int, action: (thisCell: DummyCell, nextCell: DummyCell) -> Unit): Boolean {
        if (isEmptyCell()) {
            return true
        }
        if (directionX == 0 && directionY == 0) {
            action(this, this)
            return true
        }
        val next = Point(oldPosition.x + directionX, oldPosition.y + directionY)
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
            val child = thisCell.shortcut
            AppManager.applyCustomGridChanges(context, thisCell.shortcut!!.appInfo.id, (thisCell.parent as LauncherScreenGrid).pointToPos(nextCell.oldPosition))
            thisCell.removeAllViews()
            nextCell.addView(child)
        }
    }

    fun doTranslateBy(directionX: Int, directionY: Int, value: Float): Boolean {
        return doRecursionPass(directionX, directionY) { thisCell, nextCell ->
            thisCell.shortcut?.translationX = value*directionX
            thisCell.shortcut?.translationY = value*directionY
        }
    }

    fun isEmptyCell(): Boolean {
        if (childCount == 0 || isReserved)
            return true
        return false
    }

    override fun toString(): String {
        return "\"${javaClass.simpleName}: $oldPosition empty=${isEmptyCell()} reserved=$isReserved\""
    }
}