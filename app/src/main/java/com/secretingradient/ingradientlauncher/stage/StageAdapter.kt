package com.secretingradient.ingradientlauncher.stage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.LauncherException
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.R

class StageAdapter(val launcherRootLayout: LauncherRootLayout) : RecyclerView.Adapter<StageAdapter.StageHolder>() {
    val context = launcherRootLayout.context
    val stages = launcherRootLayout.stages

    override fun getItemCount(): Int = 4

    class StageHolder(val stageRootLayout: StageRootLayout) : RecyclerView.ViewHolder(stageRootLayout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StageHolder {
        return StageHolder(
            LayoutInflater.from(context).inflate(R.layout.stage_root, parent,false) as StageRootLayout
        )
    }

    override fun onBindViewHolder(holder: StageHolder, position: Int) {
        // todo: shit method
        holder.stageRootLayout.removeAllViews()
        if (stages.getOrNull(position) == null) {
            println("inflating stage $position")
            stages.add(inflateStage(position, holder.stageRootLayout))
        } else {
            holder.stageRootLayout.addView(stages[position].stageRootLayout) // seems buggy
        }
    }

    private fun inflateStage(position: Int, parent: StageRootLayout) : BaseStage {
        val stage: BaseStage
        when (position) {
            0 -> {
                stage = MainStage(launcherRootLayout)
            }
            1 -> {
                stage = UserStage(launcherRootLayout)
            }
            2 -> {
                stage = AllAppsStage(launcherRootLayout)
            }
            3 -> {
                stage = AllWidgetsStage(launcherRootLayout)
            }
            else -> throw LauncherException("position must be in range 0..${itemCount - 1}")
        }
        stage.initInflate(parent)
        return stage
    }
}
