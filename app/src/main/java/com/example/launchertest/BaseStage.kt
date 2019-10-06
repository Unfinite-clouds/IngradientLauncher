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

    fun flipToStage(number: Int, event: DragEvent) {
        setFocus(false, event)
        launcherViewPager.currentItem = number
    }

    abstract fun adaptApp(app: AppShortcut)

    protected var isEnded = false
    protected var hasFocus = false
        private set

    private fun setFocus(value: Boolean, event: DragEvent) {
        if (hasFocus != value) {
            hasFocus = value
            if (hasFocus) onFocus(event) else onFocusLost(event)
        }
    }

    abstract fun startDrag(v: View)

    abstract fun onFocus(event: DragEvent)

    abstract fun onFocusLost(event: DragEvent)

    abstract fun onDragEnded()

    private fun endDrag() {
        isEnded = true
        onDragEnded()
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        if (v == null)
            return false

        when (event?.action) {

            DragEvent.ACTION_DRAG_STARTED -> {
                if (isEnded)
                    isEnded = false
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                setFocus(true, event)
            }

            DragEvent.ACTION_DRAG_LOCATION -> {}

            DragEvent.ACTION_DRAG_EXITED -> {}

            DragEvent.ACTION_DROP -> {}

            DragEvent.ACTION_DRAG_ENDED -> {
                if (!isEnded) {
                    setFocus(false, event)
                    endDrag()
                }
            }
        }
        return true
    }
}