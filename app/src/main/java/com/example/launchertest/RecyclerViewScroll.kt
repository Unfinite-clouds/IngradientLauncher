package com.example.launchertest

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

val SCROLL_ZONE = toPx(40).toInt()
val SCROLL_DX = 10

class RecyclerViewScroll : RecyclerView, Runnable, View.OnDragListener {
    var startPos: Int = -1
    var destPos: Int = -1
    var scrollDirection = 0
    var stopPoint = PointF()
    var isFirstDrag = true
    var dragShortcut: AppShortcut? = null
    init {
//        setOnDragListener(this)
    }
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        if (!(v is DummyCell || v == this))
            return false

        if (event?.action != DragEvent.ACTION_DRAG_STARTED)
            println(v)
        else {
            println("2")
            return true
        }

        when (event?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {}

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (v is DummyCell) {
                    translate(0f)
                    destPos = getPosition(v)
                    translate(100f)
                }
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                if (v == this)
                    translate(0f)
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                if (v != this)
                    return false
                if (isFirstDrag) isFirstDrag = false else dragShortcut?.dismissMenu()



            }

            DragEvent.ACTION_DRAG_ENDED -> stopDragScroll()
        }
        return false
    }

    fun checkAndScroll(dragX: Float, dragY: Float) {
        when {
            dragX > width - SCROLL_ZONE -> {
                scrollDirection = +1
                startDragScroll()
                stopPoint.set(dragX, dragY)
            }
            dragX < SCROLL_ZONE -> {
                scrollDirection = -1
                startDragScroll()
                stopPoint.set(dragX, dragY)
            }
            else -> stopDragScroll()
        }
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
            getAppAtPosition(pos)?.translationX = value*direction*-1
        }
    }

    private fun getAppAtPosition(position: Int): AppShortcut? {
        return (findViewHolderForAdapterPosition(position) as? ScrollStage.AppShortcutHolder)?.cell?.shortcut
    }

    private fun getPosition(v: View): Int {
        return getChildAdapterPosition(v)
    }

    override fun run() {
        this.scrollBy(SCROLL_DX*scrollDirection, 0)
        val t = findChildViewUnder(stopPoint.x, stopPoint.y)
        if (t != null) {
            destPos = getPosition(t)
            translate(100f)
        }
        this.handler.post(this)
    }

    private fun startDragScroll() {
        stopDragScroll()
        handler.post(this)
    }

    private fun stopDragScroll() {
        handler.removeCallbacks(this)
    }
}