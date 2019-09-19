package com.example.launchertest.try_grid

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.example.launchertest.R


class DummyCell : LinearLayout, DragListener {
    companion object {
        const val STATE_EMPTY = 0
        const val STATE_FILLED = 1
    }
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val bgcolor = Color.argb(40,0,0,0)
    private var side = Point(0,0)

    lateinit var position: Point

    init {
        clipChildren = false
        setBackgroundColor(bgcolor)
    }

    var state: Int = 0
    val slideAnimation = object : Runnable {
        override fun run() {
            println("slideAnimation")
        }
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        state = STATE_FILLED
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        state = STATE_EMPTY
    }

    override fun onDragStarted() {

    }

    override fun onDragEntered() {
        setBackgroundResource(R.drawable.bot_gradient)

        if (state == STATE_FILLED) {
            println("anim1_start")
            Handler().postDelayed(slideAnimation, 1500)

//            val anim = ObjectAnimator.ofFloat(this.getChildAt(0), View.TRANSLATION_Y, toPx(-30))
//            anim.duration = 300
//            anim.start()
        }
    }

    override fun onDragLocationChanged(x: Float, y: Float){
        // remember that origin of coordinate system is [left, top]
        if (y>x) side = if (y>height-x) Point(0,1) else Point(-1,0)
        else side = if (y>height-x) Point(1,0) else Point(0,-1)
        println(side)

    }

    override fun onDragExited() {
        setBackgroundColor(bgcolor)
    }

    override fun onDragEnded() {
        setBackgroundColor(bgcolor)
    }

    fun a() {
    }
}