package com.example.launchertest

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout


class DummyCell : FrameLayout {

    val parentGrid
        get() = parent as LauncherPageGrid
    lateinit var relativePosition: Point // the position within one ScreenGrid (not considering page number)
    var position: Int = -1
    private val bgcolor = Color.argb(40,0,0,0)
    var shortcut: AppShortcut?
        get() = getChildAt(0) as? AppShortcut
        set(value) {if (value != null) addView(value) else removeAllViews()}

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(bgcolor)
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

    fun defaultState() {
        shortcut?.translationX = 0f
        shortcut?.translationY = 0f
        setBackgroundColor(bgcolor)
    }

    override fun onViewAdded(child: View?) {
        if (childCount > 1) {
            throw LauncherException("$this can only have 1 child")
        }
        child?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        super.onViewAdded(child)
    }

    fun moveShortcutIntoCell(newCell: DummyCell) {
        val shortcutTemp = shortcut
        if (shortcutTemp != null) {
            this.removeAllViews()
            newCell.shortcut = shortcutTemp
            AppManager.applyCustomGridChanges(context, newCell.position, shortcutTemp.appInfo.id)
        }
    }

    // TODO: move those shits to DragCustomGrid [start]
    private fun doRecursionPass(directionX: Int, directionY: Int, action: (thisCell: DummyCell, nextCell: DummyCell) -> Unit): Boolean {
        if (isEmptyCell()) {
            return true
        }
        if (directionX == 0 && directionY == 0) {
            action(this, this)
            return true
        }
        val next = Point(relativePosition.x + directionX, relativePosition.y + directionY)
        val nextCell: DummyCell? = (parent as LauncherPageGrid).getCellAt(next)
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
    // TODO: move those shits to DragCustomGrid [end]

    fun isEmptyCell(): Boolean {
        if (childCount == 0)
            return true
        return false
    }

    override fun toString(): String {
        return "${javaClass.simpleName}: ${hashCode().toString(16)} $position empty=${isEmptyCell()}"
    }
}