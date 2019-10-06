package com.example.launchertest

import android.content.Context
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BaseStage(val context: Context) : View.OnDragListener {
    val launcherViewPager = (context as MainActivity).launcherViewPager
    protected abstract val stageLayoutId: Int

    open fun inflateAndAttach(rootLayout: ViewGroup) {
        LayoutInflater.from(context).inflate(stageLayoutId, rootLayout, true)
    }

    fun flipToStage(number: Int) {
        onFocusLost()
        launcherViewPager.currentItem = number
    }

    abstract fun adaptApp(app: AppShortcut)

    protected var isEnded = false
    protected var hasFocus = false

    abstract fun startDrag(v: View)

    open fun onFocused(event: DragEvent) {
        hasFocus = true
        isEnded = false
    }

    open fun onFocusLost() {
        hasFocus = false
    }

    open fun endDrag() {
        isEnded = true
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        if (v == null)
            return false

        when (event?.action) {

            DragEvent.ACTION_DRAG_STARTED -> {}

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (!hasFocus)
                    onFocused(event)
            }

            DragEvent.ACTION_DRAG_LOCATION -> {}

            DragEvent.ACTION_DRAG_EXITED -> {}

            DragEvent.ACTION_DROP -> {}

            DragEvent.ACTION_DRAG_ENDED -> {
                if (!isEnded) {
                    onFocusLost()
                    endDrag()
                }
            }
        }
        return true
    }
}