package com.example.launchertest.try_grid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.launchertest.AppInfo
import com.example.launchertest.R
import com.example.launchertest.getAllAppsList
import kotlinx.android.synthetic.main.activity_try_grid.*
import kotlin.random.Random


class TryGridActivity : AppCompatActivity() {
    lateinit var allApps: ArrayList<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_grid)

        allApps = getAllAppsList(this)

        fillGrid(try_grid)
    }

    private fun fillGrid(grid: LauncherScreenGrid) {
        for (i in 0 until (0.5*grid.childCount).toInt()) {
            val appInfo = getAllAppsList(this)[Random.nextInt(getAllAppsList(this).size)]
            val shortcut = AppShortcut(this, appInfo)
            shortcut.setOnLongClickListener(try_grid)
            grid.addViewTo(shortcut, i%grid.columnCount, i/grid.columnCount)
        }
    }

}
