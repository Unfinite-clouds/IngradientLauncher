@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.element.AppInfo
import com.secretingradient.ingradientlauncher.element.AppView
import kotlinx.android.synthetic.main.research_layout.*
import kotlinx.android.synthetic.main.research_layout.view.*
import java.util.*
import kotlin.math.abs


class ResearchActivity : AppCompatActivity() {

    lateinit var recyclerView: MyRecyclerView
    val maxValue = 400
    var value = 0
        set(value) {
            field = value
            seekBar.progress = (field.toFloat() / maxValue * 100).toInt()
            editText.setText(field.toString())
            recyclerView.itemAnimator?.moveDuration = field.toLong()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.research_layout)

        AppManager.loadAllApps(this)

        recyclerView = research_recycler_view

        val apps = AppManager.allApps.values.toList()
        for (i in 0..15) {
            recyclerView.list.add(i, apps[i])
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    value = (progress.toFloat() / 100 * maxValue).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        editText.setOnEditorActionListener { v, actionId, event ->
            v as EditText
            val text = v.text.toString()
            value = if (text != "") text.toInt() else 0
            hideKeyboard()

            return@setOnEditorActionListener true
        }
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

}


class MyRoot : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val recyclerBounds = Rect()
        research_recycler_view.getHitRect(recyclerBounds)
        return false
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        println("${javaClass.simpleName}")
        return super.onTouchEvent(e)
    }
}


class MyLinearLayout : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        println("${javaClass.simpleName}")
        return super.onTouchEvent(e)
    }
}


class MyRecyclerView : RecyclerView {
    val list = mutableListOf<AppInfo>()
    var itemTouchHelper: ItemTouchHelper
    var selectedVH: BaseViewHolder? = null

    init {
        this.adapter = MyAdapter()
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        itemTouchHelper = ItemTouchHelper(TouchHelper())
        itemTouchHelper.attachToRecyclerView(this)
        itemAnimator?.moveDuration = 150
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                children.forEach { view ->
                    val x = view.left + view.width / 2
                    val a = 1f - (abs(width / 2f - x) / width * 2f)
                    view.alpha = DecelerateInterpolator().getInterpolation(a)
                }
            }
        })

    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    inner class MyAdapter : Adapter<BaseViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val frame = LayoutInflater.from(context).inflate(R.layout.research_item, parent, false) as ViewGroup
            frame.getChildAt(0).apply {
                this
            }
            return BaseViewHolder(frame)
        }

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            holder.app.appInfo = list[position]
        }

        override fun onViewAttachedToWindow(holder: BaseViewHolder) {
            // restore translations, cause of Google's ItemTouchHelper.RecoverAnimation doesn't do it sometimes
            holder.itemView.translationX = 0f
            holder.itemView.translationY = 0f
            holder.app.animatorScale.start()
            super.onViewAttachedToWindow(holder)
        }
    }


    inner class TouchHelper : ItemTouchHelper.Callback() {
        private val duration = 500L
        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
            return makeMovementFlags(
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            )
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
            Collections.swap(list, viewHolder.adapterPosition, target.adapterPosition)
            recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            if (viewHolder is BaseViewHolder) {
                this@MyRecyclerView.clipChildren = false
                (this@MyRecyclerView.parent as ViewGroup).clipChildren = false
            }
            selectedVH = viewHolder as? BaseViewHolder

            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
            // we need to wait until the RecoverAnimation ends
            postDelayed(duration) {
                this@MyRecyclerView.clipChildren = true
                (this@MyRecyclerView.parent as ViewGroup).clipChildren = true
            }
        }

        override fun getAnimationDuration(recyclerView: RecyclerView, animationType: Int, animateDx: Float, animateDy: Float): Long {
            return duration
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val r = Rect(0, 0, width, height)
        if (ev.action == MotionEvent.ACTION_UP && selectedVH != null) {
            if (!r.contains(ev.x.toInt(), ev.y.toInt())) {
                adapter?.notifyItemRemoved(selectedVH!!.adapterPosition)
                return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        println("${javaClass.simpleName}")
        return super.onTouchEvent(e)
    }


    class BaseViewHolder(itemView: ViewGroup) : ViewHolder(itemView) {
        val app = itemView.getChildAt(0) as AppView
    }
}