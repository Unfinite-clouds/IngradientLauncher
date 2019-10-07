package com.example.launchertest

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.util.AttributeSet
import android.view.DragEvent
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewScroll : RecyclerView, Runnable {
    companion object {
        val SCROLL_ZONE = toPx(40).toInt()
        val SCROLL_DX = 10
    }

    var apps: MutableList<String> = mutableListOf()
    private var dragPos = -1
    private var scrollDirection = 0
    private var stopPoint: PointF? = null
    private var isScrolling = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun startDragWith(cell: DummyCell) {
        dragPos = getPosition(cell)
    }

    fun startDrag() {
        dragPos = -1
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

    private fun getAppAtPosition(position: Int): AppView? {
        return (findViewHolderForAdapterPosition(position) as? ScrollStage.AppShortcutHolder)?.cell?.app
    }

    private fun getPosition(v: DummyCell): Int {
        val pos = getChildAdapterPosition(v)
        return pos
//        return if (pos != -1) pos else throw LauncherException("can't get position. adapter pos == $pos for $v")
    }

    private fun move(from: Int, to: Int) {
        val t = apps.removeAt(from)
        apps.add(to, t)
        adapter?.notifyItemMoved(from, to)
    }

    private fun remove(position: Int) {
        apps.removeAt(position)
        adapter?.notifyItemRemoved(position)
    }

    private fun insert(appId: String, position: Int) {
        apps.add(position, appId)
        adapter?.notifyItemInserted(position)
    }

    fun moveOrInsertDragged(toCell: DummyCell, appInfo: AppInfo?) {
        if (isScrolling)
            return
        val to = getPosition(toCell)
        if (to == -1)
            return
        if (dragPos != -1) {
            val from = dragPos
            move(from, to)
        } else {
            if (appInfo == null) throw LauncherException("trying to insert to cell $toCell with null appInfo")
            insert(appInfo.id, to)
        }
        dragPos = to
    }

    fun removeDragged() {
        if (dragPos != -1) {
            remove(dragPos)
            dragPos = -1
        }
    }

    override fun dispatchDragEvent(event: DragEvent): Boolean {
        checkAndScroll(PointF(event.x, event.y))
        return super.dispatchDragEvent(event)
    }

    private val scrollHandler = Handler()
    override fun run() {
        println("$stopPoint, $scrollDirection")
        this.scrollBy(SCROLL_DX*scrollDirection, 0)
/*        synchronized(this) {
            if (stopPoint != null) {
                val t = findChildViewUnder(stopPoint!!.x, stopPoint!!.y)
                if (t != null) {
                    moveOrInsertDragged(t as DummyCell, null)
                }
            }
        }*/
        this.scrollHandler.post(this)
    }

    fun startDragScroll(scrollDirection: Int, stopPoint: PointF? = null) {
        if (!isScrolling) {
            isScrolling = true
            println("start")
            this.scrollDirection = scrollDirection
            this.stopPoint = stopPoint
            scrollHandler.post(this)
        }
    }

    fun stopDragScroll() {
        if (isScrolling) {
            isScrolling = false
            println("stop")
            stopPoint = null
            scrollHandler.removeCallbacks(this)
        }
    }
}