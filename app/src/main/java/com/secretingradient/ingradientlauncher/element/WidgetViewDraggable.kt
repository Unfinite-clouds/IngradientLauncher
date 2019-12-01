package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.view.View
import com.secretingradient.ingradientlauncher.data.WidgetInfo
import com.secretingradient.ingradientlauncher.drag.Draggable
import com.secretingradient.ingradientlauncher.drag.Hoverable

class WidgetViewDraggable(context: Context, widgetInfo: WidgetInfo) : WidgetView(context, widgetInfo), Draggable, Hoverable {
    override fun onDragStarted() {
    }

    override fun onDragEnded() {
    }

    override fun onDragMoved() {
    }

    override fun onHoverIn(draggedView: View) {
    }

    override fun onHoverOut(draggedView: View) {
    }

    override fun onHoverMoved(draggedView: View, pointLocal: IntArray) {
    }

    override fun onHoverEnd(draggedView: View) {
    }
}