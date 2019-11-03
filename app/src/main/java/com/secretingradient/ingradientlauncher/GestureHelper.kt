package com.secretingradient.ingradientlauncher

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

enum class Gesture {
    TAP_UP,
    EXACTLY_DOUBLE_TAP_DOWN, // ?
    FLING_UP,
    SCROLL_X_MOVE,
    SCROLL_Y_MOVE,
    WANDERING_MOVE,
}

class GestureHelper(context: Context) {

    private val gestureListener = GestureHelperListener()

    private val gestureDetector = GestureDetector(context, gestureListener)

    var gesture: Gesture? = null

    var doOnLongClick: () -> Unit = {}

    private var slopOvercame = false

    private var isDownEventCaught = false
    
    private var wasLongPress = false

    private var scrollDirection: Gesture? = null

    fun onTouchEvent(event: MotionEvent) {
        val action = event.action
        if (wasLongPress && action != MotionEvent.ACTION_DOWN) {
            return
        }
        if (action == MotionEvent.ACTION_MOVE && !slopOvercame) {
            gesture = Gesture.WANDERING_MOVE
        }
        gestureDetector.onTouchEvent(event)
        if (action != MotionEvent.ACTION_DOWN && !isDownEventCaught) {
            throw LauncherException("can't recognize gesture, because onDown event was not caught")
        }
    }

    private fun reset() {
        gesture = null
        scrollDirection = null
        isDownEventCaught = false
        slopOvercame = false
        wasLongPress = false
    }

    private inner class GestureHelperListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            reset()
            isDownEventCaught = true
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            if (scrollDirection == null) {
                slopOvercame = true
                scrollDirection = if (dx > dy) Gesture.SCROLL_X_MOVE else Gesture.SCROLL_Y_MOVE
            }
            gesture = scrollDirection
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            gesture = Gesture.FLING_UP
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            gesture = Gesture.TAP_UP
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            gesture = Gesture.EXACTLY_DOUBLE_TAP_DOWN
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            // this prevents case when gesture == TAP_UP after long press
            if (!slopOvercame) {
                gesture = null
                wasLongPress = true
                doOnLongClick()
            }
        }
    }
}