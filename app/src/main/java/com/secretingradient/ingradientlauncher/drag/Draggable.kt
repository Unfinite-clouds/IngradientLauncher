package com.secretingradient.ingradientlauncher.drag

interface Draggable {
    fun onDragStarted(event: DragTouchEvent)
    fun onDragEnded(event: DragTouchEvent)
    fun onDragMoved(event: DragTouchEvent)
}
