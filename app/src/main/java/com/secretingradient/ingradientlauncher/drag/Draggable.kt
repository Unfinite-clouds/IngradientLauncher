package com.secretingradient.ingradientlauncher.drag

import android.view.View

interface Draggable {
    fun onDragStarted()
    fun onDragEnded()
    fun onDragMoved()
}

/*
abstract class OnDragListener : View.OnDragListener {
    override fun onDrag(v: View?, event: DragEvent?): Boolean {}
    open fun onDragStarted(v: View) {}
    open fun onDragEnded(v: View) {}
    open fun onDrag(v: View) {}
    open fun onHoverIn(v: View) {}
    open fun onHoverOut(v: View) {}
    open fun onHover(v: View) {}
}
*/

interface DraggableHandler <ViewType: View> : Draggable {
    val v: ViewType
    val context
        get() = v.context
    override fun onDragStarted() {}
    override fun onDragEnded() {}
    override fun onDragMoved() {}
}

/*class AppViewDraggable(context: Context) : ImageView(context), Draggable {
    override fun onDragStarted() {}
    override fun onDragEnded() {}
    override fun onMoved() {}
}

class AppDraggableHandler(override val v: ImageView) : DraggableHandler<ImageView> {
    override fun onDragStarted() {}
    override fun onDragEnded() {}
    override fun onMoved() {}
}*/
