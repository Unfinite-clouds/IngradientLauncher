package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.*
import kotlinx.android.synthetic.main.stage_0_main_screen.view.*
import kotlinx.android.synthetic.main.stage_1_custom_grid.view.*
import kotlinx.android.synthetic.main.stage_2_all_apps.view.*

class StageAdapter(context: Context) : RecyclerView.Adapter<StageHolder>() {
    private lateinit var context: Context
    val stages = mutableListOf<ViewGroup>()
    lateinit var stage_root: ViewGroup

    init {
        for (page in 0..3) {
            this.context = context
            stages.add(createStage(context, page))
        }
    }

    override fun getItemCount(): Int = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StageHolder {
        context = parent.context
        return StageHolder(context, View(context))
    }

    override fun onBindViewHolder(holder: StageHolder, page: Int) {
        val root = (holder.itemView as ViewGroup)

        (stages[page].parent as? ViewGroup)?.removeAllViews()
        holder.itemView.removeAllViews()
        root.addView(stages[page])
    }

    fun createStage(context: Context, page: Int): ViewGroup {
        val stage_root = LayoutInflater.from(context).inflate(R.layout.stage, null,false) as ViewGroup
        when (page) {
            0 -> {
                // Scroll stage
                View.inflate(context, R.layout.stage_0_main_screen, stage_root)
                for (i in 0..10) {
                    //fill first 10 apps
                    stage_root.iconContainer.addView(
                        IconFactory(
                            context,
                            getPrefs(context).getInt(Preferences.MAIN_SCREEN_ICONS_COUNT, -1))
                            .createIcon(AppManager.getApp(AppManager.getSortedApps()[i])!!))
                }
            }
            1 -> {
                // Screen stage
                View.inflate(context, R.layout.stage_1_custom_grid, stage_root)
                stage_root.custom_grid_vp.adapter = CustomGridAdapter()
                (context as MainActivity).stageCustomGrid = stage_root.custom_grid_vp
            }
            2 -> {
                // AllApps stage
                View.inflate(context, R.layout.stage_2_all_apps, stage_root)
                stage_root.all_apps_vp.adapter = AllAppsGridAdapter()
            }
        }
        stage_root.setBackgroundResource(R.color.Vignette)
        return stage_root
    }
}


class StageHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView)

