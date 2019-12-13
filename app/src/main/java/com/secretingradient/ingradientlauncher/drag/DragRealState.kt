package com.secretingradient.ingradientlauncher.drag

import android.view.View
import android.view.ViewGroup

class DragRealState {
    var parent: ViewGroup? = null
        private set
    var translationX = 0f
        private set
    var translationY = 0f
        private set
    var x = 0f
        private set
    var y = 0f
        private set

    fun saveState(v: View?) {
        parent = v?.parent as? ViewGroup
        translationX = v?.translationX ?: 0f
        translationY = v?.translationY ?: 0f
        x = v?.x ?: 0f
        y = v?.y ?: 0f
    }

    fun loadState(v: View?) {
        if (v == null)
            return
        v.translationX = translationX
        v.translationY = translationY
    }
}