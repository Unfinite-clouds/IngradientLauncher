package com.example.launchertest.try_grid

interface Draggable {
    fun onDragStarted()
    fun onEntered()
    fun onExited()
    fun onLocationChanged(x: Float, y: Float)
}