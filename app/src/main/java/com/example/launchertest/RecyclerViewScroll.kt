package com.example.launchertest

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView

val SCROLL_ZONE = toPx(40).toInt()
val SCROLL_DX = 10

class RecyclerViewScroll : RecyclerView, Runnable {
    var startPos: Int = -1
    var destPos: Int = -1
    var scrollDirection = 0
    var stopPoint: PointF? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun dragStarted(cell: DummyCell) {
        startPos = getPosition(cell)
        destPos = -1
        scrollDirection = 0
        stopPoint = null
        stopDragScroll()
        resetTranslate()
    }

    fun dragStartedWithNew() {
        startPos = childCount
        destPos = -1
        scrollDirection = 0
        stopPoint = null
        stopDragScroll()
        resetTranslate()
    }

    fun checkAndScroll(dragPoint: PointF) {
        when {
            dragPoint.x > width - SCROLL_ZONE -> {
                startDragScroll(+1, dragPoint)
            }
            dragPoint.x < SCROLL_ZONE -> {
                startDragScroll(-1, dragPoint)
            }
            else -> stopDragScroll()
        }
    }

    fun handleTranslate(cell: DummyCell) {
        translate(0f)
        destPos = getPosition(cell)
        translate(100f)
    }

    private fun translate(value: Float) {
        if (startPos == destPos || destPos == -1)
            return
        val direction = if (startPos < destPos) 1 else -1
        var pos = startPos
        while (pos != destPos) {
            pos+=direction
            getAppAtPosition(pos)?.translationX = value*direction*-1
        }
    }

    private fun getAppAtPosition(position: Int): AppShortcut? {
        return (findViewHolderForAdapterPosition(position) as? ScrollStage.AppShortcutHolder)?.cell?.shortcut
    }

    private fun getPosition(v: DummyCell): Int {
        return getChildAdapterPosition(v)
    }



    override fun run() {
        this.scrollBy(SCROLL_DX*scrollDirection, 0)
        synchronized(this) {
            if (stopPoint != null) {
                val t = findChildViewUnder(stopPoint!!.x, stopPoint!!.y)
                if (t != null) {
                    handleTranslate(t as DummyCell)
                }
            }
        }
        this.handler.post(this)
    }

    fun resetTranslate() {
        children.forEach {
            it as DummyCell
            it.shortcut?.translationX = 0f
        }
    }
    fun startDragScroll(scrollDirection: Int, stopPoint: PointF? = null) {
        stopDragScroll()
        this.scrollDirection = scrollDirection
        this.stopPoint = stopPoint
        handler.post(this)
    }

    fun stopDragScroll() {
        handler.removeCallbacks(this)
    }
}