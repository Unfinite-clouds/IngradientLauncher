package com.secretingradient.ingradientlauncher.stage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.BoundViewHolder
import com.secretingradient.ingradientlauncher.LauncherException
import com.secretingradient.ingradientlauncher.R

class StageAdapter(val context: Context) : RecyclerView.Adapter<BoundViewHolder>() {

    override fun getItemCount(): Int = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoundViewHolder {
        return BoundViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.stage,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BoundViewHolder, position: Int) {
        if (holder.boundPosition != position) {
            println("binding position $position")
            val rootLayout = holder.itemView as ViewGroup
            rootLayout.removeAllViews()
            val stage = createStage(context, position)
            stage.inflateAndAttach(rootLayout)
            holder.boundPosition = position
        }
    }

    private fun createStage(context: Context, position: Int) : BaseStage {
        val stage: BaseStage
        when (position) {
            0 -> {
                // Scroll starter
                stage = MainStage(context)
            }
            1 -> {
                // Custom starter
                stage = UserStage(context)
            }
            2 -> {
                // AllApps starter
//                stage = AllAppsStage(context)
                stage = UserStage(context)
            }
            3 -> {
                // Widgets starter
                stage = AllAppsStage(context)
            }
            else -> throw LauncherException("position must be in range 0..${itemCount - 1}")
        }
        return stage
    }
}
