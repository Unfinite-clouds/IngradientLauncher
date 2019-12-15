package com.secretingradient.ingradientlauncher.drag

interface Hoverable {
    fun onHoverIn(event: DragTouchEvent)
    fun onHoverOut(event: DragTouchEvent)
    fun onHoverMoved(event: DragTouchEvent)
    fun onHoverEnded(event: DragTouchEvent)
}

open class HoverableImpl : Hoverable{
    override fun onHoverIn(event: DragTouchEvent) {}
    override fun onHoverOut(event: DragTouchEvent) {}
    override fun onHoverMoved(event: DragTouchEvent) {}
    override fun onHoverEnded(event: DragTouchEvent) {}
}