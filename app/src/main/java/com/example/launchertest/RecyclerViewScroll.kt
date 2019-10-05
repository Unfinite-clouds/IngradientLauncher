package com.example.launchertest

import android.content.Context
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

val SCROLL_ZONE = toPx(40).toInt()
val SCROLL_DX = 10

class RecyclerViewScroll : RecyclerView, Runnable {
    var startPos: Int = -1
    var destPos: Int = -1
    var scrollDirection = 0
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onDragEvent(event: DragEvent?): Boolean {
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
        return true
    }

    private fun listenDrag(event: DragEvent?) {

    }

    override fun dispatchDragEvent(event: DragEvent?): Boolean {
        return super.dispatchDragEvent(event)
    }

    fun translate(value: Float) {
        translate(startPos, destPos, value)
    }

    private fun translate(startPos: Int, destPos: Int, value: Float) {
        if (startPos == destPos || destPos == -1)
            return
        val direction = if (startPos < destPos) 1 else -1
        var pos = startPos
        while (pos != destPos) {
            pos+=direction
            getViewAtPosition(pos)?.translationX = value*direction*-1
        }
    }

    private fun getViewAtPosition(position: Int): AppShortcut? {
        return (findViewHolderForAdapterPosition(position) as? ScrollStage.AppShortcutHolder)?.cell?.shortcut
    }

    private fun getPosition(v: AppShortcut): Int {
        return getChildAdapterPosition(v.parent as View)
    }

    override fun run() {
        this.scrollBy(SCROLL_DX*scrollDirection, 0)
        destPos = getChildLayoutPosition(getChildAt(childCount-2))
        translate(100f*scrollDirection)
        this.handler.post(this)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        println("$l $t $oldl $oldt")
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