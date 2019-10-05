package com.example.launchertest

import android.content.Context
import android.util.AttributeSet
import android.view.DragEvent
import androidx.recyclerview.widget.RecyclerView

val SCROLL_ZONE = toPx(40).toInt()
val SCROLL_DX = 10

class RecyclerViewScroll : RecyclerView, Runnable {
    var startPos: Int = -1
    var destPos: Int = -1
    var scrollDirection = 0
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private fun listenDrag(event: DragEvent?) {
        when (event?.action) {
            DragEvent.ACTION_DRAG_LOCATION -> {
                when {
                    event.x > width - SCROLL_ZONE -> {
                        scrollDirection = +1
                        startDragScroll()
                    }
                    event.x < SCROLL_ZONE -> {
                        scrollDirection = -1
                        startDragScroll()
                    }
                    else -> stopDragScroll()
                }
            }
            DragEvent.ACTION_DRAG_ENDED -> stopDragScroll()
        }
    }

    override fun dispatchDragEvent(event: DragEvent?): Boolean {
        listenDrag(event)
        return super.dispatchDragEvent(event)
    }

    override fun run() {
        this.scrollBy(SCROLL_DX*scrollDirection, 0)
        this.handler.post(this)
    }

    @Synchronized
    private fun startDragScroll() {
        stopDragScroll()
        handler.post(this)
    }

    @Synchronized
    private fun stopDragScroll() {
        handler.removeCallbacks(this)
    }
}