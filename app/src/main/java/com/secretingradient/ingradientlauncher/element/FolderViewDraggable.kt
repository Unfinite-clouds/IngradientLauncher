package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.view.View
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.data.AppInfo
import com.secretingradient.ingradientlauncher.drag.DraggableElement
import com.secretingradient.ingradientlauncher.drag.DraggableElementI
import com.secretingradient.ingradientlauncher.drag.Hoverable
import java.lang.ref.WeakReference

class FolderViewDraggable(context: Context, vararg apps: AppInfo, delegate: DraggableElement = DraggableElement())
    : FolderView(context, *apps), Hoverable, DraggableElementI by delegate {

    init {
        delegate.ref = WeakReference(this)
    }

    constructor(context: Context, apps: Collection<AppInfo>) : this(context) {
        addApps(apps)
    }

    override fun onDragEnded() {
    }

    override fun onDragMoved() {
    }

    override fun onHoverIn(draggedView: View) {
        if (draggedView !is AppView) return
        addApps(draggedView.info!!)
        val pos = getPagedPosition()
        if (apps.size > 2)
            addAction { dataset.put(pos, this.info, true, false) }
    }

    override fun onHoverOut(draggedView: View) {
        if (draggedView !is AppView) return
        if (draggedView.info == apps[apps.size - 1]) {
            removeApp(apps.size - 1)
            val pos = getPagedPosition()
            if (apps.size < 2) {
                val app = transformToApp()
                addAction { dataset.put(pos, app.info!!, true, false) }
            } else {
                addAction { dataset.put(pos, this.info, true, false) }
            }
        }
    }

    override fun onHoverMoved(draggedView: View, pointLocal: IntArray) {
    }

    override fun onHoverEnd(draggedView: View) {
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