package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
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

    lateinit var recyclerView: RecyclerView
    val list = mutableListOf<AppInfo>()
    val maxValue = 400
    var value = 0
        set(value) {
            field = value
            seekBar.progress = (field.toFloat() / maxValue * 100).toInt()
            editText.setText(field.toString())
            recyclerView.itemAnimator?.moveDuration = field.toLong()
        }

    lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.research_layout)

        AppManager.loadAllApps(this)

        val apps = AppManager.allApps.values.toList()
        for (i in 0..15) {
            list.add(i, apps[i])
        }

        recyclerView = research_recycler_view

        recyclerView.adapter = MyAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        itemTouchHelper = ItemTouchHelper(TouchHelper())
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.itemAnimator?.moveDuration = 150
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.children.forEachIndexed { i, view ->
                    val x = view.left + view.width / 2
                    val a = 1f - (abs(recyclerView.width / 2f - x) / recyclerView.width * 2f)
                    view.alpha = DecelerateInterpolator().getInterpolation(a)
                }
            }
        })


        research_btn.setOnClickListener {
            Collections.swap(list, 0, 1)
            recyclerView.adapter?.notifyItemMoved(0, 1)
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

    inner class MyAdapter : RecyclerView.Adapter<BaseViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val frame = layoutInflater.inflate(R.layout.research_item, parent, false) as ViewGroup
            frame.getChildAt(0).apply {
            }
            return BaseViewHolder(frame)
        }

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            holder.app.appInfo = list[position]
        }

        override fun onViewAttachedToWindow(holder: BaseViewHolder) {
            holder.app.animatorScale.start()
            super.onViewAttachedToWindow(holder)
        }

    }

    class BaseViewHolder(itemView: ViewGroup) : RecyclerView.ViewHolder(itemView) {
        val app = itemView.getChildAt(0) as AppView
    }

    inner class TouchHelper : ItemTouchHelper.Callback() {
        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return makeMovementFlags(
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            )
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            Collections.swap(list, viewHolder.adapterPosition, target.adapterPosition)
            recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (viewHolder is BaseViewHolder) {
                viewHolder.itemView.setBackgroundColor(Color.GRAY)
            }

            super.onSelectedChanged(viewHolder, actionState)
        }
    }

    inner class ItemDragListener() : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            println(v)
            if (v is RecyclerView)
                return false
            return true
        }

    }
}

class MyRoot : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val recyclerBounds = Rect()
        research_recycler_view.getHitRect(recyclerBounds)
        println(recyclerBounds)
        return false
    }
}
