@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.data.DataKeeper
import com.secretingradient.ingradientlauncher.drag.DragContext
import com.secretingradient.ingradientlauncher.drag.DragController
import com.secretingradient.ingradientlauncher.drag.Draggable
import com.secretingradient.ingradientlauncher.drag.Hoverable
import com.secretingradient.ingradientlauncher.sensor.BaseSensor


class ResearchActivity : AppCompatActivity() {
    lateinit var dk: DataKeeper
    lateinit var context: Context
    lateinit var controller: DragController
    val stages = mutableListOf<TestStage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout._research_layout)
        controller = DragController(findViewById(R.id.drag_layer))
        stages.add(findViewById(R.id.stage1))
        stages.add(findViewById(R.id.stage2))
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}



class MyRoot : FrameLayout {
    val controller
        get() = (context as ResearchActivity).controller

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!controller.dispatchTouchEvent(event))
            return super.dispatchTouchEvent(event)
        else
            return true
    }
}



class TestStage(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val dragContext = object : DragContext() {
        override var canStartDrag: Boolean = true
        override val contentView: ViewGroup
            get() = this@TestStage
    }
    lateinit var snapLayout: SnapLayout
    val sensors = mutableListOf<BaseSensor>()

    init {
        setOnLongClickListener {
            val controller = (context as ResearchActivity).controller
            controller.dragContext = this.dragContext
            controller.dragEnabled = true
            true
        }
//        addView(FolderWindow(context, dataset, 120))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        snapLayout = getChildAt(0) as SnapLayout
        sensors.add(getChildAt(1) as BaseSensor)

        populate()
    }

    private fun populate() {
        for (i in 0 until 3) {
            val v_child = AppDraggable(context).apply { setImageResource(R.drawable.ic_launcher_background); setBackgroundColor(Color.BLACK) }
            v_child.setSnapLayoutParams(i, 1, 1)
            snapLayout.addView(v_child)
        }
        val v = snapLayout[1]
        v.scaleX = 1.5f
        v.rotationX = 50f
        v.translationY = 150f
    }

}


/*class AppDraggableHandler(override val v: ImageView) : DraggableHandler<ImageView> {
    override fun onDragStarted() {
    }

    override fun onDragMoved() {
    }
}

class AppHoverableHandler(override val v: ImageView) : HoverableHandler<ImageView> {
    override fun onHoverIn(draggedView: View) {
        v.setBackgroundColor(Color.YELLOW)
//        val activity = (context as ResearchActivity)
//        activity.controller.dragContext = activity.stages[1].dragContext
    }
    override fun onHoverOut(draggedView: View) {
        v.setBackgroundColor(Color.BLACK)
    }
}

class SnapHoverableHandler(override val v: SnapLayout) : HoverableHandler<SnapLayout> {
    override fun onHoverMoved(draggedView: View) {
    }
}*/

class AppDraggable(context: Context) : ImageView(context), Draggable, Hoverable {
    override fun onDragStarted() {
        setBackgroundColor(Color.RED)
    }

    override fun onDragEnded() {
        setBackgroundColor(Color.BLACK)
        println("onDragEnded")
    }

    override fun onDragMoved() {
    }

    override fun onHoverIn(draggedView: View) {
        setBackgroundColor(Color.YELLOW)
    }
    override fun onHoverOut(draggedView: View) {
        setBackgroundColor(Color.BLACK)
        println("onHoverOut")
    }

    override fun onHoverMoved(draggedView: View) {
    }

}

class SnapLayoutHover(context: Context, attrs: AttributeSet) : SnapLayout(context, attrs), Hoverable {
    override fun onHoverIn(draggedView: View) {
        setBackgroundColor(Color.WHITE)
    }

    override fun onHoverOut(draggedView: View) {
        setBackgroundColor(Color.BLACK)
    }

    override fun onHoverMoved(draggedView: View) {
    }

}




class SnapViewHolder : SnapElementImpl {
    constructor(v: View) : super(v)
    constructor(v: View, pos: Int, snapWidth: Int, snapHeight: Int) : super(v, pos, snapWidth, snapHeight)
}

class AppViewElement(context: Context, snapElementImpl: SnapElementImpl = SnapElementImpl()) : ImageView(context), SnapElement by snapElementImpl {
    init {
        snapElementImpl.v = this
        setImageResource(R.drawable.ic_launcher_background)
    }
}

interface SnapElement {
    var position: Int
    var snapWidth: Int
    var snapHeight: Int
}

open class SnapElementImpl : SnapElement {
    private lateinit var v_field: View
    var v: View
        get() = v_field
        set(value) {
            v_field = value
            if (v.layoutParams !is SnapLayout.SnapLayoutParams)
                v.layoutParams = SnapLayout.SnapLayoutParams(-1, -1, -1).also { println("WARNING: init without SnapLayoutParams") }
        }
    override var position: Int
        get() = snap().position
        set(value) { snap().position = value }
    override var snapWidth: Int
        get() = snap().snapWidth
        set(value) { snap().snapWidth = value }
    override var snapHeight: Int
        get() = snap().snapHeight
        set(value) { snap().snapHeight = value }
    constructor()
    constructor(v: View) {
        this.v = v
    }
    constructor(v: View, pos: Int, snapWidth: Int, snapHeight: Int) {
        v.layoutParams = SnapLayout.SnapLayoutParams(pos, snapWidth, snapHeight)
        this.v = v
    }

    private fun snap(): SnapLayout.SnapLayoutParams {
        return v.layoutParams as SnapLayout.SnapLayoutParams
    }
}

class ItemDragger : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
    }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}