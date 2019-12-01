package com.secretingradient.ingradientlauncher.drag

import android.view.View
import com.secretingradient.ingradientlauncher.Launcher
import com.secretingradient.ingradientlauncher.LauncherActivity
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.data.Data
import com.secretingradient.ingradientlauncher.data.Dataset
import com.secretingradient.ingradientlauncher.data.Info
import com.secretingradient.ingradientlauncher.stage.PagedStage2
import java.lang.ref.WeakReference

interface DraggableElementI : Draggable {
    val launcher : Launcher
    val dataset: Dataset<Data, Info>

    fun addAction(f: ()->Unit)

    fun getPagedPosition(): Int
}

class DraggableElement : DraggableElementI {
    lateinit var ref: WeakReference<View>
    val v: View?
        get() = ref.get()
    val context
        get() = v?.context
    override val launcher
        get() = (context as LauncherActivity).launcher
    override val dataset: Dataset<Data, Info>
        get() {
            val stage = launcher.currentStage as PagedStage2
            return stage.dataset
        }

    override fun onDragStarted() {
        if (v != null && v!!.parent !is SnapLayout) return
        val pos = getPagedPosition()
        addAction { dataset.remove(pos, false) }
    }

    override fun onDragEnded() {
    }

    override fun onDragMoved() {
    }

    override fun addAction(f: ()->Unit) {
        launcher.currentStage.dragContext?.pendingActions?.add(f)
    }

    override fun getPagedPosition(): Int {
        if (v != null && v!!.parent !is SnapLayout) return -1
        val stage = launcher.currentStage as PagedStage2
        return stage.getPagedPosition((v!!.layoutParams as SnapLayout.SnapLayoutParams).position, v!!.parent as SnapLayout)
    }

}