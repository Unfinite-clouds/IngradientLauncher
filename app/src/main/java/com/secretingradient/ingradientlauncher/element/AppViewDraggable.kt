package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.view.View
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.data.AppInfo
import com.secretingradient.ingradientlauncher.drag.Draggable
import com.secretingradient.ingradientlauncher.drag.DraggableElement
import com.secretingradient.ingradientlauncher.drag.DraggableElementI
import com.secretingradient.ingradientlauncher.drag.Hoverable
import java.lang.ref.WeakReference

class AppViewDraggable(context: Context, info: AppInfo? = null, delegate: DraggableElement = DraggableElement())
    : AppView(context, info), Hoverable, DraggableElementI by delegate {

    init {
        delegate.ref = WeakReference(this)
    }

    override fun onDragEnded() {
    }

    override fun onDragMoved() {
    }

    override fun onHoverIn(draggedView: View) {
        if (draggedView !is AppView) return
        val pos = getPagedPosition()
        val folder = transformToFolder(draggedView)
        launcher.dragController.setHovered(folder, draggedView as Draggable)
        addAction { dataset.put(pos, folder.info, true, false) }
    }

    override fun onHoverOut(draggedView: View) {}

    override fun onHoverMoved(draggedView: View, pointLocal: IntArray) {}

    override fun onHoverEnd(draggedView: View) {}

    private fun transformToFolder(draggedAppView: AppView): FolderViewDraggable {
        val parentFrom = parent as SnapLayout
        val folder = FolderViewDraggable(context, this.info!!)
        parentFrom.removeView(this)
        parentFrom.addNewView(folder, (this.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
        return folder
    }
}