package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.withTranslation
import com.secretingradient.ingradientlauncher.OnPreDispatchListener

class StageRootLayout : ConstraintLayout {
    lateinit var stage: BaseStage
    var shouldIntercept = false
    var overlayView: View? = null
    private val touchPoint = PointF()
    var preDispatchListener: OnPreDispatchListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (overlayView != null) {
            touchPoint.set(ev.x, ev.y)
            invalidate()
        }
        preDispatchListener?.onPreDispatch(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return shouldIntercept
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        overlayView?.let {
            canvas?.withTranslation(touchPoint.x - it.width / 2, touchPoint.y - it.height / 2) {
                it.draw(canvas)
            }
        }
    }
}