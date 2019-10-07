package com.example.launchertest

import android.content.Context
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BaseStage(val context: Context) : View.OnDragListener {
    val launcherViewPager = (context as MainActivity).launcherViewPager
    lateinit var rootLayout: ViewGroup
    protected abstract val stageLayoutId: Int

    open fun inflateAndAttach(rootLayout: ViewGroup) {
        this.rootLayout = LayoutInflater.from(context).inflate(stageLayoutId, rootLayout, true) as ViewGroup
    }

    fun flipToStage(number: Int, event: DragEvent) {
        setFocus(false, event)
        launcherViewPager.currentItem = number
    }

    abstract fun adaptApp(app: AppView)

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

    abstract fun onDragEnded(event: DragEvent)

    private fun endDrag(event: DragEvent) {
        isEnded = true
        onDragEnded(event)
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
                    val owner = getOwner(event)
                    if (owner != this)
                        owner.endDrag(event)
                    endDrag(event)
                    setFocus(false, event)
                }
            }
        }
        return true
    }

    data class DragState(val app: AppView, val owner: BaseStage)

    fun getParcelApp(event: DragEvent): AppView {
        return (event.localState as DragState).app
    }
    fun getOwner(event: DragEvent): BaseStage {
        return (event.localState as DragState).owner
    }

    fun isMyEvent(event: DragEvent): Boolean {
        return getOwner(event) == this
    }
}