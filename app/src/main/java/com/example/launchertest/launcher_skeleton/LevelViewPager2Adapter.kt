package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.launchertest.IconFactory
import com.example.launchertest.LauncherPreferences
import com.example.launchertest.R
import com.example.launchertest.getAllAppsList
import kotlinx.android.synthetic.main.level.view.*
import kotlinx.android.synthetic.main.level_screens.view.*
import kotlinx.android.synthetic.main.level_scroll.view.*

class LevelViewPager2Adapter : RecyclerView.Adapter<LevelHolder>() {

    private lateinit var context: Context
    companion object {
        val colors = intArrayOf(
            android.R.color.transparent,
            android.R.color.holo_red_light,
            android.R.color.holo_blue_dark,
            android.R.color.holo_purple,
            android.R.color.black
        )
    }

    override fun getItemCount(): Int = colors.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelHolder {
        context = parent.context
        return LevelHolder(context, LayoutInflater.from(context).inflate(R.layout.level,parent,false))
    }

    override fun onBindViewHolder(levelHolder: LevelHolder, position: Int) {
        levelHolder.bind(position)
    }
}


class LevelHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
    val level = itemView as ViewGroup
    var bindedPos = -1

    fun bind(position: Int) {
        if (bindedPos != position) {
            level.removeAllViews()

            if (position == 0){
                // Scroll level
                View.inflate(context, R.layout.level_scroll, level.root)
                for (i in 0..10) {
                    //fill first 10 apps
                    level.iconContainer.addView(IconFactory(context,PreferenceManager(context).sharedPreferences.getInt(LauncherPreferences.MAIN_SCREEN_ICONS_COUNT, -1)).createIcon(getAllAppsList(context)[i]))
                }
            } else {
                // Screen level
                View.inflate(context, R.layout.level_screens, level.root)
                level.view_pager.adapter = ScreenViewPager2Adapter()
            }
            level.setBackgroundResource(LevelViewPager2Adapter.colors[position])

            bindedPos = position
        }
    }
}