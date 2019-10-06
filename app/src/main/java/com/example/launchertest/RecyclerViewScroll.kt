package com.example.launchertest

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Handler
import android.util.AttributeSet
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class RecyclerViewScroll : RecyclerView, Runnable {
    companion object {
        val SCROLL_ZONE = toPx(40).toInt()
        val SCROLL_DX = 10
    }

    var startPos: Int = -1
    var destPos: Int = -1
    var scrollDirection = 0
    var stopPoint: PointF? = null
    var apps: MutableList<String> = mutableListOf()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun dragStarted(cell: DummyCell) {
        startPos = getPosition(cell)
        dragStartedReset()
    }

    fun dragStartedWithNew(appId: String) {
        apps.add(appId)
        startPos = apps.size - 1
        adapter?.notifyItemInserted(apps.size - 1)
        dragStartedReset()
    }

    private fun dragStartedReset() {
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
//        translate(0f)
        resetTranslate()
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
            val app = getAppAtPosition(pos)
            app?.translationX = value*direction*-1
            val t = 255 / abs(startPos-destPos) * (abs(pos-startPos)-1)
            app?.icon?.colorFilter = PorterDuffColorFilter(Color.rgb(30,t, 40), PorterDuff.Mode.SRC_IN)
        }
    }

    private fun getAppAtPosition(position: Int): AppView? {
        return (findViewHolderForAdapterPosition(position) as? ScrollStage.AppShortcutHolder)?.cell?.app
    }

    private fun getPosition(v: DummyCell): Int {
        return getChildAdapterPosition(v)
    }

    private val scrollHandler = Handler()
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
        this.scrollHandler.post(this)
    }

    fun resetTranslate() {
        children.forEach {
            (it as DummyCell)
            it.app?.translationX = 0f
            it.app?.icon?.clearColorFilter()
        }
    }

    fun startDragScroll(scrollDirection: Int, stopPoint: PointF? = null) {
        stopDragScroll()
        this.scrollDirection = scrollDirection
        this.stopPoint = stopPoint
        scrollHandler.post(this)
    }

    fun stopDragScroll() {
        scrollHandler.removeCallbacks(this)
    }
}