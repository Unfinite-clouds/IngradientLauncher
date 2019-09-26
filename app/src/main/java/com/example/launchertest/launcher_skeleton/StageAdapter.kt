package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.*
import kotlinx.android.synthetic.main.stage.view.*
import kotlinx.android.synthetic.main.stage_0_main_screen.view.*
import kotlinx.android.synthetic.main.stage_1_custom_grid.view.*

class StageAdapter : RecyclerView.Adapter<StageHolder>() {
    private lateinit var context: Context

    override fun getItemCount(): Int = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StageHolder {
        context = parent.context
        return StageHolder(context, LayoutInflater.from(context).inflate(R.layout.stage, parent,false))
    }

    override fun onBindViewHolder(stageHolder: StageHolder, position: Int) {
        stageHolder.bind(position)
    }
}


class StageHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
    val stage = itemView as ViewGroup
    var bindedPos = -1

    fun bind(position: Int) {
        if (bindedPos != position) {
            stage.removeAllViews()

            if (position == 0){
                // Scroll stage
                View.inflate(context, R.layout.stage_0_main_screen, stage.root)
                for (i in 0..10) {
                    //fill first 10 apps
                    stage.iconContainer.addView(IconFactory(context,
                        getPrefs(context).getInt(Preferences.MAIN_SCREEN_ICONS_COUNT, -1))
                        .createIcon(AppManager.getApp(AppManager.getSortedApps()[i])!!))
                }
            } else {
                // Screen stage
                View.inflate(context, R.layout.stage_1_custom_grid, stage.root)
                stage.custom_grid_vp.adapter = CustomGridAdapter()
            }
            stage.setBackgroundResource(R.color.colorVignette)

            bindedPos = position
        }
    }
}