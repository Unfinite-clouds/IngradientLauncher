package com.example.launchertest.try_grid

interface DragListener {
    fun onDragStarted()
    fun onDragEntered()
    fun onDragExited()
    fun onDragLocationChanged(x: Float, y: Float)
    fun onDragEnded()
}