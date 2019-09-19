package com.example.launchertest.try_grid

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.example.launchertest.R


class DummyCell : LinearLayout, Draggable {
    companion object {
        const val STATE_EMPTY = 0
        const val STATE_FILLED = 1
    }
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

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
        if (state == STATE_FILLED) {
            println("a_start")
            Handler().postDelayed(slideAnimation, 1500)
            setBackgroundResource(R.drawable.bot_gradient)

            val anim = TranslateAnimation()
            anim.duration = 300f
            anim.interpolator = DecelerateInterpolator()
            anim.fillAfter = true
            this.startAnimation(anim)
//            val anim = ObjectAnimator.ofFloat(this.getChildAt(0), View.TRANSLATION_Y, toPx(-30))
//            anim.duration = 300
//            anim.start()
        }
    }

    override fun onDragLocationChanged(x: Float, y: Float){
        val side: String

        // remember that origin of coordinate system is [left, top]
        if (y>x) side = if (y>height-x) "bottom" else "left"
        else side = if (y>height-x) "right" else "top"
    }

    override fun onDragExited() {
        setBackgroundColor(Color.argb(40,0,0,0))
    }

    override fun onDragEnded() {
        setBackgroundColor(Color.argb(40,0,0,0))
    }
}