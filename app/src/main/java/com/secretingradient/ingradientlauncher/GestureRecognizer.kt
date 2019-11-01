package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.sqrt

enum class Gesture {
    NULL,
    DOWN,
    TAP,
    SINGLE_TAP, // todo dont work
    DOUBLE_TAP,  // todo dont work
    SHOW_PRESS,
    LONG_PRESS, // todo dont work
    SCROLL_X,
    SCROLL_Y,
    WANDERING,
    FLING,
    UP_NO_GESTURE,
    CANCEL;
    fun isMove() = this == SCROLL_X || this == SCROLL_Y || this == WANDERING
    fun isUp() = this == FLING || this == UP_NO_GESTURE || this == TAP
}

class GestureRecognizer(val context: Context, var onLongListener: View.OnLongClickListener? = null) {

    enum class ScrollDirection {
        DIRECTION_X,
        DIRECTION_Y
    }

    private val onLongPressRunnable = Runnable { if (isDownEventCaught) { gestureListener.onLongPress(null); onLongListener?.onLongClick(null)} }  // todo (v = null)

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

    var gesture: Gesture = Gesture.NULL

    var touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        set(value) {
            field = value
            touchSlopSqr = value * value
        }

    var touchSlopSqr = touchSlop * touchSlop

    var longPressTimeOut = ViewConfiguration.getLongPressTimeout()

    private var scrollDirectionRecognized = false

    private val downPoint = PointF()

    private var isDownEventCaught = false

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

    fun recognizeTouchEvent(event: MotionEvent): Gesture {
        gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_MOVE -> onMove(event)
            MotionEvent.ACTION_UP -> {
                if (gesture != Gesture.FLING && gesture != Gesture.TAP) gesture = Gesture.UP_NO_GESTURE
                isDownEventCaught = false
                handler.removeCallbacks(onLongPressRunnable)
            }
            MotionEvent.ACTION_CANCEL -> {
                gesture = Gesture.CANCEL
                isDownEventCaught = false
                handler.removeCallbacks(onLongPressRunnable)
            }
        }
        if (gesture == Gesture.NULL)
            println("WARNING: recognizeTouchEvent returned Gesture.NULL")

        return gesture
    }

    private fun onMove(event: MotionEvent) {
        if (!isDownEventCaught) {
            throw LauncherException("can't recognize onMove gesture, because onDown event was not caught")
        }

        if (!scrollDirectionRecognized) {
            computeDistances(event)
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
            } else {
                gesture = Gesture.WANDERING
            }
        }
    }

    fun reset() {
        gesture = Gesture.NULL
        scrollDirectionRecognized = false
        distanceX = 0f
        distanceY = 0f
        distanceSqr = 0f
        flingVelocityX = 0f
        flingVelocityY = 0f
        isDownEventCaught = false
        isLongTapCanceled = false
        handler.removeCallbacks(onLongPressRunnable)
    }

    fun relocateDonwPoint(pointF: PointF) {
        relocateDonwPoint(pointF.x, pointF.y)
    }

    fun relocateDonwPoint(x: Float, y: Float) {
        downPoint.set(x, y)
        isDownEventCaught = true
    }

    fun cancelLongTap() {
        isLongTapCanceled = true
        handler.removeCallbacks(onLongPressRunnable)
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
            isDownEventCaught = true
            handler.postDelayed(onLongPressRunnable, longPressTimeOut.toLong())
            downPoint.set(event.x, event.y)
            gesture = Gesture.DOWN
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, _dx: Float, _dy: Float): Boolean {
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (!isDownEventCaught) {
                println("WARNING: can we recognize onFling gesture? OnDown event was not caught")
            }
            flingVelocityX = velocityX
            flingVelocityY = velocityY
            gesture = Gesture.FLING
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            gesture = Gesture.TAP
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            gesture = Gesture.DOUBLE_TAP
            return true
        }

        override fun onShowPress(e: MotionEvent?) {
            gesture = Gesture.SHOW_PRESS
        }

        override fun onLongPress(e: MotionEvent?) {
            gesture = Gesture.LONG_PRESS
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            gesture = Gesture.SINGLE_TAP
            return true
        }
    }
}