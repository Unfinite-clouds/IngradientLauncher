package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.graphics.withTranslation

class StageRootLayout : LinearLayout {
    lateinit var stage: BaseStage
    var shouldIntercept = false
    var overlayView: View? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return shouldIntercept
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (this.overlayView != null) {
            canvas?.withTranslation(overlayView!!.translationX, overlayView!!.translationY) {
                this@StageRootLayout.overlayView?.draw(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

/*    private val hitRect = Rect()
    private val p = Point()
    private var lastHited: View? = null
    var hitedView: View? = null

    private fun getHitView(x: Int, y: Int): View {
        p.set(x, y)

        if (lastHited != null && testHit(lastHited!!)) {
            return lastHited!!
        }

        children.forEach {
            if (testHit(it))
                return it
        }

        return this
    }

    private fun testHit(v: View): Boolean {
        v.getHitRect(hitRect)
        return hitRect.contains(p.x, p.y)
    }*/
}