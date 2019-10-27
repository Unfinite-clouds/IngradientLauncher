package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.sqrt

enum class Gesture {
    SINGLE_TAP,
    DOUBLE_TAP,
    SINGLE_TAP_CONFIRMED,
//    SHOW_PRESS,
    LONG_PRESS,
    SCROLL_X,
    SCROLL_Y,
    FLING,
}

class GestureRecognizer(val context: Context) {

    enum class ScrollDirection {
        DIRECTION_X,
        DIRECTION_Y
    }

    private val onLongTap = Runnable { if (isPressed) gestureListener.onLongPress(null) }

    private val handler = Handler()

    private val gestureListener = GestureRecognizerListener()

    private val gestureDetector = GestureDetector(context, gestureListener, handler).apply { setIsLongpressEnabled(false) }

    var onScrollDirectionRecognizedListener: OnScrollDirectionRecognizedListener? = null
    var onScrollDirectionRecognized: (scrollDirection: ScrollDirection) -> Unit
        set(value) {
            onScrollDirectionRecognizedListener = OnScrollDirectionRecognizedListener(value)
        }
        get() = {
            onScrollDirectionRecognizedListener
        }

    var gesture: Gesture? = null

    var touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        set(value) {
            field = value
            touchSlopSqr = value * value
        }

    var touchSlopSqr = touchSlop * touchSlop

    var longTapTimeOut = 200L
        private set

    private var scrollDirectionRecognized = false

    private val downPoint = PointF()

    private var isPressed = false

    private var isLongTapCanceled = false

    var distanceX = 0f
        private set
    var distanceY = 0f
        private set
    var distanceSqr = 0f
        private set
    var flingVelocityX = 0f
        private set
    var flingVelocityY = 0f
        private set
    val distance
        get() = sqrt(distanceSqr)

    fun onTouchEvent(event: MotionEvent) {
        gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                reset()
                isPressed = true
                handler.postDelayed(onLongTap, longTapTimeOut)
            }
            MotionEvent.ACTION_UP -> {
                isPressed = false; handler.removeCallbacks(onLongTap)
            }
        }
    }

    fun reset() {
        gesture = null
        scrollDirectionRecognized = false
        distanceX = 0f
        distanceY = 0f
        distanceSqr = 0f
        flingVelocityX = 0f
        flingVelocityY = 0f
        isPressed = false
        isLongTapCanceled = false
        handler.removeCallbacks(onLongTap)
    }

    fun relocateDonwPoint(pointF: PointF) {
        downPoint.set(pointF)
    }

    fun relocateDonwPoint(x: Float, y: Float) {
        downPoint.set(x, y)
    }

    fun cancelLongTap() {
        isLongTapCanceled = true
        handler.removeCallbacks(onLongTap)
    }

    interface OnScrollDirectionRecognizedListener {
        fun onScrollDirectionRecognized(scrollDirection: ScrollDirection)
    }

    fun OnScrollDirectionRecognizedListener(f: (scrollDirection: ScrollDirection) -> Unit): OnScrollDirectionRecognizedListener {
        return object : OnScrollDirectionRecognizedListener {
            override fun onScrollDirectionRecognized(scrollDirection: ScrollDirection) {
                f(scrollDirection)
            }
        }
    }

    private fun computeDistances(event: MotionEvent): Float {
        distanceX = abs(event.x - downPoint.x)
        distanceY = abs(event.y - downPoint.y)
        distanceSqr = distanceX * distanceX + distanceY * distanceY
        return distanceSqr
    }

    private inner class GestureRecognizerListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            reset()
            downPoint.set(event.x, event.y)
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, _dx: Float, _dy: Float): Boolean {
            if (!scrollDirectionRecognized) {
                computeDistances(e2)
                if (distanceSqr >= touchSlopSqr) {
                    cancelLongTap()
                    scrollDirectionRecognized = true
                    val scrollDirection: ScrollDirection
                    if (distanceX > distanceY) {
                        gesture = Gesture.SCROLL_X
                        scrollDirection = ScrollDirection.DIRECTION_X
                    } else {
                        gesture = Gesture.SCROLL_Y
                        scrollDirection = ScrollDirection.DIRECTION_Y
                    }
                    onScrollDirectionRecognizedListener?.onScrollDirectionRecognized(scrollDirection)
                }
            }
            println(gesture)
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            flingVelocityX = velocityX
            flingVelocityY = velocityY
            gesture = Gesture.FLING
            println(gesture)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            gesture = Gesture.SINGLE_TAP
            println(gesture)
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            gesture = Gesture.DOUBLE_TAP
            println(gesture)
            return true
        }

/*        override fun onShowPress(e: MotionEvent?) {
            gesture = Gesture.SHOW_PRESS
            println(gesture)
        }*/

        override fun onLongPress(e: MotionEvent?) {
            gesture = Gesture.LONG_PRESS
            println(gesture)
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            gesture = Gesture.SINGLE_TAP_CONFIRMED
            println(gesture)
            return true
        }
    }
}