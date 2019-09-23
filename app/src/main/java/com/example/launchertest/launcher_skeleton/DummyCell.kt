package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import com.example.launchertest.DragListener
import com.example.launchertest.LauncherException
import com.example.launchertest.R
import com.example.launchertest.randomColor


class DummyCell : LinearLayout, DragListener {
    constructor(context: Context?, x: Int, y: Int) : super(context) {
        position = Point(x,y)
        layoutParams = GridLayout.LayoutParams(GridLayout.spec(position.y),GridLayout.spec(position.x))
        layoutParams.width = 0
        layoutParams.height = 0
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    var isReserved: Boolean = false
    lateinit var position: Point
//    private val bgcolor = Color.argb(40,0,0,0)
    private val bgcolor = randomColor()

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(bgcolor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onViewAdded(child: View?) {
        if (childCount > 1) {
            throw LauncherException("${javaClass.simpleName} can only have 1 child")
        }
        child?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        super.onViewAdded(child)
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        isReserved = false
    }

    fun getShortcut(): AppShortcut? {
        return getChildAt(0) as AppShortcut?
    }


    override fun onDragStarted() {

    }

    override fun onDragEntered() {
        setBackgroundResource(R.drawable.bot_gradient)
    }

    override fun onDragLocationChanged(x: Float, y: Float){

    }

    override fun onDragExited() {
        setBackgroundColor(bgcolor)
    }

    override fun onDragEnded() {
        setBackgroundColor(bgcolor)
        isReserved = false
        getShortcut()?.translationX = 0f
        getShortcut()?.translationY = 0f
    }

    private fun doRecursionPass(directionX: Int, directionY: Int, action: (thisCell: DummyCell, nextCell: DummyCell) -> Unit): Boolean {
        if (isEmptyCell()) {
            return true
        }
        if (directionX == 0 && directionY == 0) {
            action(this, this)
            return true
        }
        val next = Point(position.x + directionX, position.y + directionY)
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
            val child = thisCell.getShortcut()
            thisCell.removeAllViews()
            nextCell.addView(child)
        }
    }

    fun doTranslateBy(directionX: Int, directionY: Int, value: Float): Boolean {
        return doRecursionPass(directionX, directionY) { thisCell, nextCell ->
            thisCell.getShortcut()?.translationX = value*directionX
            thisCell.getShortcut()?.translationY = value*directionY
        }
    }

    fun isEmptyCell(): Boolean {
        if (childCount == 0 || isReserved)
            return true
        return false
    }

    override fun toString(): String {
        return "${javaClass.simpleName}: $position empty=${isEmptyCell()} reserved=$isReserved"
    }
}