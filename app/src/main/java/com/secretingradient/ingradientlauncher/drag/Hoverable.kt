package com.secretingradient.ingradientlauncher.drag

import android.view.View


interface Hoverable {
    fun onHoverIn(draggedView: View)
    fun onHoverOut(draggedView: View)
    fun onHoverMoved(draggedView: View, pointLocal: IntArray)
    fun onHoverEnd(draggedView: View)
}

interface HoverableHandler <ViewType: View> : Hoverable {
    val v: ViewType
    val context
        get() = v.context
    override fun onHoverIn(draggedView: View) {}
    override fun onHoverOut(draggedView: View) {}
    override fun onHoverMoved(draggedView: View, pointLocal: IntArray) {}
}
