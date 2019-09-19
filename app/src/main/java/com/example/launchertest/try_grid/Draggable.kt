package com.example.launchertest.try_grid

interface Draggable {
    fun onDragStarted()
    fun onDragEntered()
    fun onDragExited()
    fun onDragLocationChanged(x: Float, y: Float)
    fun onDragEnded()
}