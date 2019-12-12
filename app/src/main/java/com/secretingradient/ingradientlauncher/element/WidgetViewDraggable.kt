package com.secretingradient.ingradientlauncher.element

import android.content.Context
import com.secretingradient.ingradientlauncher.data.WidgetInfo
import com.secretingradient.ingradientlauncher.drag.DragTouchEvent
import com.secretingradient.ingradientlauncher.drag.Draggable
import com.secretingradient.ingradientlauncher.drag.Hoverable

class WidgetViewDraggable(context: Context, widgetInfo: WidgetInfo) : WidgetView(context, widgetInfo), Draggable, Hoverable {
    override fun onDragStarted(event: DragTouchEvent) {
    }

    override fun onDragEnded(event: DragTouchEvent) {
    }

    override fun onDragMoved(event: DragTouchEvent) {
    }

    override fun onHoverIn(event: DragTouchEvent) {
    }

    override fun onHoverOut(event: DragTouchEvent) {
    }

    override fun onHoverMoved(event: DragTouchEvent) {
    }

    override fun onHoverEnded(event: DragTouchEvent) {
    }

}