package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.getAllAppsList
import com.example.launchertest.randomColor

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
//        LayoutInflater.from(context).inflate(R.layout.screen_custom_grid, parent, true)
        return ScreenHolder(context, LauncherScreenGrid(context, 4, 3))
    }


    override fun onBindViewHolder(screenHolder: ScreenHolder, position: Int) {
        if (screenHolder.adapterPosition != position || screenHolder.layoutPosition != position || screenHolder.adapterPosition != screenHolder.layoutPosition)
            println("Bind with bug APos: ${screenHolder.adapterPosition}, LPos: ${screenHolder.layoutPosition}")
        screenHolder.bind(position)
    }
}


class ScreenHolder(private val context: Context, val grid: LauncherScreenGrid) : RecyclerView.ViewHolder(grid) {
    var bindedPos = -1
    val width = grid.columnCount
    val height = grid.rowCount

    fun bind(position: Int) {
        if (bindedPos != position) {
            grid.clearGrid()
            // all apps
            var app: Int
            for (i in 0 until width*height) {
                app = 13+i+width*height*position
                if (app > getAllAppsList(context).size - 1)
                    break
                val appInfo = getAllAppsList(context)[app]
                // one way to fix it is add shortcuts after Grid.onLayout()
                grid.addViewTo(AppShortcut(context, appInfo), i%width, i/width)
//                grid.addView(IconFactoryGrid.createIcon(context, getAllAppsList(context)[app], 720, 1520, width, height))
            }

            // custom screen
//            for (i in 0 until width*height) {
//                app = i+width*height*position
//                if (app > getAllAppsList(context).size - 1)
//                    break
//                grid.addView(IconFactoryGrid.createIcon(context, getAllAppsList(context)[app], 720, 1520, width, height))
//            }
            grid.setBackgroundColor(randomColor())
            bindedPos = position
        }
    }
}