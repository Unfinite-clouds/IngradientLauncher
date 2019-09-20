package com.example.launchertest.try_grid

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
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

    fun getShortcut(): ImageView? {
        return getChildAt(0) as ImageView?
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

    private fun doRecursionPass(directionX: Int, directionY: Int, action: (nextCell: DummyCell) -> Unit): Boolean {
        if (isEmptyCell()) {
            return true
        }
        if (directionX == 0 && directionY == 0) {
            action(this)
            return true
        }
        val next = Point(position.x + directionX, position.y + directionY)
        val nextCell: DummyCell? = (parent as LauncherScreenGrid).getCellAt(next)
        if (nextCell?.doRecursionPass(directionX, directionY, action) == true) {
            action(nextCell)
            return true
        }
        return false
    }

    fun canMoveBy(directionX: Int, directionY: Int): Boolean {
        return doRecursionPass(directionX, directionY) {}
    }

    fun doMoveBy(directionX: Int, directionY: Int): Boolean {
        return doRecursionPass(directionX, directionY) { nextCell ->
            val child = getChildAt(0)
            removeAllViews()
            nextCell.addView(child)
        }
/*        if (isEmptyCell()) {
            return true
        }
        val next = Point(position.x + directionX, position.y + directionY)
        val nextCell: DummyCell? = (parent as LauncherScreenGrid).getCellAt(next)
        if (nextCell?.doMoveBy(directionX, directionY) == true) {
            val child = getChildAt(0)
            removeAllViews()
            nextCell.addView(child)
            return true
        }
        return false*/
    }

    fun doTranslateBy(directionX: Int, directionY: Int, value: Float): Boolean {
        return doRecursionPass(directionX, directionY) {
            getShortcut()?.translationX = value*directionX
            getShortcut()?.translationY = value*directionY
        }
/*        if (isEmptyCell()) {
            return true
        }
        if (directionX == 0 && directionY == 0) {
            getShortcut()?.translationX = value*directionX
            getShortcut()?.translationY = value*directionY
            return true
        }
        val next = Point(position.x + directionX, position.y + directionY)
        val nextCell: DummyCell? = (parent as LauncherScreenGrid).getCellAt(next)
        if (nextCell?.doTranslateBy(directionX, directionY, value) == true) {
            getShortcut()?.translationX = value*directionX
            getShortcut()?.translationY = value*directionY
            return true
        }
        return false*/
    }

    fun isEmptyCell(): Boolean {
        if (childCount == 0 || isReserved)
            return true
        return false
    }
}