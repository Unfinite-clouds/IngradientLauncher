package com.example.launchertest.try_grid

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.example.launchertest.LauncherException
import com.example.launchertest.R


class DummyCell : LinearLayout, DragListener {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)


    var isReserved: Boolean = false
    lateinit var position: Point
    private val bgcolor = Color.argb(40,0,0,0)
    val slideAnimation = object : Runnable {
        override fun run() {
//            println("slideAnimation")
        }
    }

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(bgcolor)
    }

    override fun onViewAdded(child: View?) {
        if (childCount > 1) {
            throw LauncherException("${javaClass.simpleName} can only have 1 child")
        }
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

//        if (childCount != 0) {
//            println("anim1_start")
//            Handler().postDelayed(slideAnimation, 1500)

//            val anim = ObjectAnimator.ofFloat(this.getChildAt(0), View.TRANSLATION_Y, toPx(-30))
//            anim.duration = 300
//            anim.start()
//        }
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