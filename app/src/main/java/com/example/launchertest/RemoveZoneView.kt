package com.example.launchertest

import android.content.Context
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat

class RemoveZoneView : ImageView, View.OnDragListener{
    init {
        setOnDragListener(this)
        deactivate()
    }
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        when (event?.action) {
            DragEvent.ACTION_DRAG_STARTED -> activate()
            DragEvent.ACTION_DROP -> {
                // we just cancel this drag event marking the shortcut as "goingToRemove"
                (event.localState as Pair<DummyCell, AppShortcut>).second.goingToRemove = true
            }
            DragEvent.ACTION_DRAG_ENDED -> deactivate()
        }
        return true
    }

    private fun activate() {
        setBackgroundResource(R.color.TransparentWhite)
        setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, null))
    }

    private fun deactivate() {
        setBackgroundResource(R.color.Transparent)
        setImageDrawable(null)
    }
}