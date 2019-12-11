package com.secretingradient.ingradientlauncher.drag

interface Hoverable {
    fun onHoverIn(event: DragTouchEvent)
    fun onHoverOut(event: DragTouchEvent)
    fun onHoverMoved(event: DragTouchEvent)
    fun onHoverEnd(event: DragTouchEvent)
}
