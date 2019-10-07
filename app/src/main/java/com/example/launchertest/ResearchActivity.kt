package com.example.launchertest

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ResearchActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    val list = mutableListOf<AppInfo>()

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
            list.removeAt(2)
            recyclerView.adapter?.notifyItemMoved(2,5)
        }
    }
}

class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)