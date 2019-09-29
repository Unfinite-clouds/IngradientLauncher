package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import com.example.launchertest.AppManager
import com.example.launchertest.LauncherException


class DummyCell : LinearLayout {

    lateinit var relativePosition: Point // the position within one ScreenGrid (not considering page number)
    var position: Int = -1
    private val bgcolor = Color.argb(40,0,0,0)
    var shortcut: AppShortcut?
        get() = getChildAt(0) as? AppShortcut
        set(value) {if (value != null) addView(value) else removeAllViews()}
    var reservedShortcut: AppShortcut? = null
        private set

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

    fun moveReservedShortcutIntoCell(newCell: DummyCell) {
        val shortcutTemp = reservedShortcut
        if (shortcutTemp != null) {
            newCell.shortcut = shortcutTemp
            AppManager.applyCustomGridChanges(context, newCell.position, shortcutTemp.appInfo.id)
        }
        reservedShortcut = null
    }

    fun applyRemoveShortcut() {
        val shortcutTemp = shortcut
        if (shortcutTemp != null) {
            AppManager.applyCustomGridChanges(context, -1, shortcutTemp.appInfo.id)
            removeAllViews()
        }
    }

    fun reserveShortcut() {
        if (reservedShortcut != null)
            throw LauncherException("$this - shortcut is already reserved")
        if (shortcut == null)
            throw LauncherException("$this - nothing to reserve")
        reservedShortcut = shortcut
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
        if (childCount == 0 || reservedShortcut != null)
            return true
        return false
    }

    override fun toString(): String {
        return "\"${javaClass.simpleName}: $position empty=${isEmptyCell()} reserve=$reservedShortcut\""
    }
}