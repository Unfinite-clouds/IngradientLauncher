package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.*

class StageAdapter : RecyclerView.Adapter<StageHolder>() {
    private lateinit var context: Context
    val stages = mutableListOf<ViewGroup>()


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


}

fun createStage(context: Context, page: Int): ViewGroup {
        stage.removeAllViews()

        when (position) {
            0 -> {
                // Scroll stage
                View.inflate(context, R.layout.stage_0_main_screen, stage.root)
                for (i in 0..10) {
                    //fill first 10 apps
                    stage.iconContainer.addView(
                        IconFactory(
                            context,
                            getPrefs(context).getInt(Preferences.MAIN_SCREEN_ICONS_COUNT, -1))
                            .createIcon(AppManager.getApp(AppManager.getSortedApps()[i])!!))
                }
            }
            1 -> {
                // Screen stage
                View.inflate(context, R.layout.stage_1_custom_grid, stage.root)
                stage.custom_grid_vp.adapter = CustomGridAdapter()
                (context as MainActivity).stageCustomGrid = stage.custom_grid_vp
            }
            2 -> {
                // AllApps stage
                View.inflate(context, R.layout.stage_2_all_apps, stage.root)
                stage.all_apps_vp.adapter = AllAppsGridAdapter()
            }
        }
        bindedPos = position
        stage.setBackgroundResource(R.color.Vignette)
}