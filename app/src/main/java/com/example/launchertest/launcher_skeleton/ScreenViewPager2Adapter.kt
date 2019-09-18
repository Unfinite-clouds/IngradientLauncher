package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.IconFactoryGrid
import com.example.launchertest.R
import com.example.launchertest.getAllAppsList
import kotlinx.android.synthetic.main.screen_app_grid.view.*

class ScreenViewPager2Adapter : RecyclerView.Adapter<ScreenHolder>() {

    private lateinit var context: Context

    companion object {
        val colors = intArrayOf(
            android.R.color.holo_red_light,
            android.R.color.black,
            android.R.color.holo_blue_dark,
            android.R.color.holo_purple,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.transparent
        )
    }

    override fun getItemCount(): Int = colors.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenHolder {
        context = parent.context
        return ScreenHolder(context,LayoutInflater.from(context).inflate(R.layout.screen_app_grid, parent, false))
    }


    override fun onBindViewHolder(screenHolder: ScreenHolder, position: Int) {
        if (screenHolder.adapterPosition != position || screenHolder.layoutPosition != position || screenHolder.adapterPosition != screenHolder.layoutPosition)
            println("Bind with bug APos: ${screenHolder.adapterPosition}, LPos: ${screenHolder.layoutPosition}")
        screenHolder.bind(position)
    }
}


class ScreenHolder(private val context: Context, view: View) : RecyclerView.ViewHolder(view){
    val grid = view.app_grid
    var bindedPos = -1
    var width = 5
    var height = 6

    init {
        grid.columnCount = width
        grid.rowCount = height
    }

    fun bind(position: Int) {
        if (bindedPos != position) {
            grid.removeAllViews()

            // all apps
            var app: Int
            for (i in 0 until width*height) {
                app = i+width*height*position
                if (app > getAllAppsList(context).size - 1)
                    break
                grid.addView(IconFactoryGrid.createIcon(context, getAllAppsList(context)[app], 720, 1520, width, height))
            }

            // custom screen
            for (i in 0 until width*height) {
                app = i+width*height*position
                if (app > getAllAppsList(context).size - 1)
                    break
                grid.addView(IconFactoryGrid.createIcon(context, getAllAppsList(context)[app], 720, 1520, width, height))
            }

            (grid.parent as ViewGroup).setBackgroundResource(ScreenViewPager2Adapter.colors[position])
            bindedPos = position
        }
    }
}