package com.secretingradient.ingradientlauncher.drag

import android.view.View
import android.view.ViewGroup
import com.secretingradient.ingradientlauncher.LauncherActivity
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.stage.PagedStage2

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
    var snapPositionPaged = -1
        private set

    fun saveState(v: View?) {
        parent = v?.parent as? ViewGroup
        translationX = v?.translationX ?: 0f
        translationY = v?.translationY ?: 0f
        x = v?.x ?: 0f
        y = v?.y ?: 0f
        setSnapPositionPaged(v)
    }

    fun loadState(v: View?) {
        if (v == null)
            return
        v.translationX = translationX
        v.translationY = translationY
    }

    private fun setSnapPositionPaged(v: View?) {
        val parent = parent
        val lp = v?.layoutParams
        val c = (parent?.context as? LauncherActivity)?.launcher?.currentStage as? PagedStage2
        snapPositionPaged = if (parent is SnapLayout && c != null && lp is SnapLayout.SnapLayoutParams)
            c.getPagedPosition(lp.position, c.currentSnapLayout)
        else
            -1
    }
}