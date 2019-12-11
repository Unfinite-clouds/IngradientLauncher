package com.secretingradient.ingradientlauncher.element

import android.content.Context
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.className
import com.secretingradient.ingradientlauncher.data.AppInfo
import com.secretingradient.ingradientlauncher.drag.DragTouchEvent
import com.secretingradient.ingradientlauncher.drag.DraggableElement
import com.secretingradient.ingradientlauncher.drag.DraggableElementImpl
import com.secretingradient.ingradientlauncher.drag.Hoverable
import java.lang.ref.WeakReference

class FolderViewDraggable(context: Context, vararg apps: AppInfo, delegate: DraggableElementImpl = DraggableElementImpl())
    : FolderView(context, *apps), Hoverable, DraggableElement by delegate {

    init {
        delegate.ref = WeakReference(this)
    }

    constructor(context: Context, apps: Collection<AppInfo>) : this(context) {
        addApps(apps)
    }

    override fun onDragEnded(event: DragTouchEvent) {
    }

    override fun onDragMoved(event: DragTouchEvent) {
    }

    override fun onHoverIn(event: DragTouchEvent) {
        println("onHoverIn ${this.className()} ${event.transformMatrix}")
        val draggedView = event.draggableView
        if (draggedView is AppView) {
            addApps(draggedView.info!!)
            val pos = getPagedPosition()
            if (apps.size > 2)
                addAction2 { dataset.put(pos, this.info, true, false) }
        }
    }

    override fun onHoverOut(event: DragTouchEvent) {
        println("onHoverOut ${this.className()} ${event.transformMatrix}")
        val draggedView = event.draggableView
        if (draggedView is AppView && draggedView.info == apps[apps.size - 1]) {
            removeApp(apps.size - 1)
            val pos = getPagedPosition()
            if (apps.size < 2) {
                val app = transformToApp()
                addAction2 { dataset.put(pos, app.info!!, true, false) }
            } else {
                addAction2 { dataset.put(pos, this.info, true, false) }
            }
        }
    }

    override fun onHoverMoved(event: DragTouchEvent) {
        println("onHoverMoved ${this.className()} ${event.transformMatrix}")
    }

    override fun onHoverEnd(event: DragTouchEvent) {
        println("onHoverEnd ${this.className()} ${event.transformMatrix}")
    }

    fun transformToApp(): AppViewDraggable {
        val parent = parent as SnapLayout
        val appView = AppViewDraggable(context, apps[0])
        parent.removeView(this)
        parent.addNewView(appView, (layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
        clear()
        return appView
    }
}