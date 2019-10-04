package com.example.launchertest

import android.content.Context
import android.util.AttributeSet
import android.view.DragEvent
import androidx.recyclerview.widget.RecyclerView

val SCROLL_ZONE = toPx(40).toInt()

class RecyclerViewScroll : RecyclerView {
    inner class ScrollRunnable(private val dx: Int, private val event: DragEvent) : Runnable {
        override fun run() {
            this@RecyclerViewScroll.scrollBy(dx, 0)
//            this@RecyclerViewScroll.dispatchDragEventChildren(event)
            this@RecyclerViewScroll.handler.post(this)
        }
    }
    private var scrollRunnable: ScrollRunnable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun dispatchDragEvent(event: DragEvent?): Boolean {
        when (event?.action) {
            DragEvent.ACTION_DRAG_LOCATION -> {
                when {
                    event.x > width - SCROLL_ZONE -> { startDragScroll(10, event)}
                    event.x < SCROLL_ZONE -> { startDragScroll(-10, event)}
                    else -> stopDragScroll()
                }
            }
            DragEvent.ACTION_DRAG_ENDED -> stopDragScroll()
        }
        return super.dispatchDragEvent(event)
    }

    private fun dispatchDragEventChildren(event: DragEvent): Boolean {
        return super.dispatchDragEvent(event)
    }

    @Synchronized
    private fun startDragScroll(dx: Int, event: DragEvent) {
        stopDragScroll()
        scrollRunnable = ScrollRunnable(dx, event)
        handler.post(scrollRunnable!!)
    }

    @Synchronized
    private fun stopDragScroll() {
        if (scrollRunnable == null)
            return
        handler.removeCallbacks(scrollRunnable!!)
        scrollRunnable = null
    }
}