package com.secretingradient.ingradientlauncher.drag

import android.view.View
import com.secretingradient.ingradientlauncher.Launcher
import com.secretingradient.ingradientlauncher.LauncherActivity
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.className
import com.secretingradient.ingradientlauncher.data.Data
import com.secretingradient.ingradientlauncher.data.Dataset
import com.secretingradient.ingradientlauncher.data.Info
import com.secretingradient.ingradientlauncher.stage.PagedStage2
import java.lang.ref.WeakReference

interface DraggableElement : Draggable {
    val launcher : Launcher
    val dataset: Dataset<Data, Info>

    fun addAction1(f: ()->Unit)
    fun addAction2(f: ()->Unit)

    fun getPagedPosition(): Int
}

class DraggableElementImpl : DraggableElement {
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

    override fun onDragStarted(event: DragTouchEvent) {
        println("onDragStarted ${this.className()} ${event.transformMatrixH}")

        if (v != null && v!!.parent !is SnapLayout) return
        val pos = getPagedPosition()
        addAction1 { dataset.remove(pos, false) }
    }

    override fun onDragEnded(event: DragTouchEvent) {
        println("onDragEnded ${this.className()} ${event.transformMatrixH}")
    }

    override fun onDragMoved(event: DragTouchEvent) {
//        println("onDragMoved ${this.className()} ${event.transformMatrix}")
    }

    override fun addAction1(f: ()->Unit) {
        launcher.currentStage.dragContext?.pendingActions?.add(0, f)
    }

    override fun addAction2(f: () -> Unit) {
        launcher.currentStage.dragContext?.pendingActions?.add(1, f)
    }

    override fun getPagedPosition(): Int {
        return when (v?.parent) {
            is SnapLayout -> {
                val stage = launcher.currentStage as PagedStage2
                stage.getPagedPosition((v!!.layoutParams as SnapLayout.SnapLayoutParams).position, v!!.parent as SnapLayout)
            }
            is DragLayer -> {
                launcher.dragController.realState.snapPositionPaged
            }
            else -> -1
        }
    }

}