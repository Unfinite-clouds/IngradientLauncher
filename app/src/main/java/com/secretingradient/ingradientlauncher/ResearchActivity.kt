package com.secretingradient.ingradientlauncher

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.element.AppInfo
import com.secretingradient.ingradientlauncher.element.AppView
import kotlinx.android.synthetic.main.research_layout.*
import java.lang.ref.WeakReference
import java.util.*


class ResearchActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    val list = mutableListOf<AppInfo>()
    val maxValue = 400
    var value = 0
        set(value) {
            field = value
            seekBar.progress = (field.toFloat()/maxValue*100).toInt()
            editText.setText(field.toString())
            recyclerView.itemAnimator?.moveDuration = field.toLong()
        }

    val holders = mutableListOf<WeakReference<BaseViewHolder>>()
    val checkHandler = Handler()
    val checkRunnable = object : Runnable {
        override fun run() {
            holders.forEach {
                if (it.get() == null) {
                    println("removing VH")
                    holders.remove(it)
                }
                checkHandler.post(this)
            }
        }
    }

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
        recyclerView.setRecyclerListener {println("${(it.itemView as AppView).appInfo.label} Recycled") }
        ItemTouchHelper(DragHelperCallback()).attachToRecyclerView(recyclerView)

        checkHandler.post(checkRunnable)

        research_btn.setOnClickListener {
            Collections.swap(list, 0, 1)
            recyclerView.adapter?.notifyItemMoved(0,1)
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
            return BaseViewHolder(AppView(this@ResearchActivity, AppInfo("","")).apply {
                layoutParams = ViewGroup.LayoutParams(120,120)
                println("Create VH")
            }).apply { holders.add(WeakReference(this)) }
        }

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            (holder.itemView as AppView).appInfo = list[position]
            println("Bind VH $position, ${holder.itemView.id}")
        }

    }

    inner class DragHelperCallback : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, ItemTouchHelper.UP or ItemTouchHelper.DOWN)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            Collections.swap(list, viewHolder.adapterPosition, target.adapterPosition)
            recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            println("onSwiped")
        }

    }
}

class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)