package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.example.launchertest.R

class RemoveZoneView : ImageView, View.OnDragListener{
    init {
        setOnDragListener(this)
        inactivate()
    }
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        when (event?.action) {
            DragEvent.ACTION_DRAG_STARTED -> activate()
            DragEvent.ACTION_DROP ->
                (event.localState as? DummyCell)?.removeShortcut()
            DragEvent.ACTION_DRAG_ENDED -> inactivate()
        }
        return true
    }

    private fun activate() {
        setBackgroundResource(R.color.TransparentWhite)
        setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, null))
    }

    private fun inactivate() {
        setBackgroundResource(R.color.Transparent)
        setImageDrawable(null)
    }
}