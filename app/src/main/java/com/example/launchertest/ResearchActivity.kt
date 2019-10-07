package com.example.launchertest

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.research_layout.*

class ResearchActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    val list = mutableListOf<AppInfo>()
    var duration = 300
        set(value) {
            field = value
            seekBar.progress = value/400*100
            editText.setText(duration.toString())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.research_layout)

        AppManager.loadAllApps(this)

        val apps = AppManager.allApps.values.toList()
        for (i in 0..15) {
            list.add(i, apps[i])
        }

        recyclerView = findViewById(R.id.research_recycler_view)

        recyclerView.adapter = object : RecyclerView.Adapter<BaseViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return BaseViewHolder(AppView(this@ResearchActivity, AppInfo("","")).apply {
                    layoutParams = ViewGroup.LayoutParams(120,120)
                })
            }

            override fun getItemCount() = list.size

            override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
                (holder.itemView as AppView).appInfo = list[position]
            }

        }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)



        val btn = findViewById<Button>(R.id.research_btn)
        btn.setOnClickListener {
//            list.removeAt(0)
            recyclerView.adapter?.notifyItemMoved(0,1)
            recyclerView.scrollToPosition(0)//-120,0, AccelerateDecelerateInterpolator(), duration )
//            recyclerView.smoothScrollBy(-120,0, null, 250 )
//            recyclerView.adapter?.notifyItemInserted(0)

        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    duration = progress/100*400
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        editText.setOnEditorActionListener { v, actionId, event ->
            duration = v.text.toString().toInt()
            return@setOnEditorActionListener true
        }
    }
}

class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)