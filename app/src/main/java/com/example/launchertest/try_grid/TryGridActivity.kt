package com.example.launchertest.try_grid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.launchertest.AppInfo
import com.example.launchertest.R
import com.example.launchertest.getAllAppsList
import com.example.launchertest.launcher_skeleton.AppShortcut
import com.example.launchertest.launcher_skeleton.LauncherScreenGrid
import kotlinx.android.synthetic.main.activity_try_grid.*


class TryGridActivity : AppCompatActivity() {
    lateinit var allApps: ArrayList<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_grid)

        allApps = getAllAppsList(this)

        fillGrid(try_grid)

        try_grid_request_btn.setOnClickListener { try_grid.requestLayout() }
    }

    private fun fillGrid(grid: LauncherScreenGrid) {
        var a = 0
        for (i in 0 until (0.5*grid.childCount).toInt()) {
            a++
            val appInfo = getAllAppsList(this)[a+17]
            val shortcut = AppShortcut(this, appInfo)
//            View.inflate(this, R.layout.shortcut_icon, grid)
            shortcut.setOnLongClickListener(try_grid)
            grid.addViewTo(shortcut, i%grid.columnCount, i/grid.columnCount)
        }
    }

}
