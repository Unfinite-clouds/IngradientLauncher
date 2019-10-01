package com.example.launchertest

import android.content.ClipData
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.example.launchertest.launcher_skeleton.AppShortcut
import com.example.launchertest.launcher_skeleton.StageAdapter

class MainActivity : AppCompatActivity(), View.OnLongClickListener {
    lateinit var stageCustomGrid: ViewPager2
    lateinit var stages: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

//        Storable.deleteFile(this, Storable.CUSTOM_GRID_APPS)
        AppManager.loadAllApps(this)

        if (getPrefs(this).getBoolean(Preferences.FIRST_LOAD, true))
            loadDefaultPreferences()

/*
        val allApps = AppManager.allApps
        var i = 0
        for (app in allApps) {
            if (i > 12) break
            AppManager.applyCustomGridChanges(this, i, app.key)
            i++
        }
*/

        stages = findViewById<ViewPager2>(R.id.root_viewpager)
        stages.adapter = StageAdapter(this)
        stages.orientation = ViewPager2.ORIENTATION_VERTICAL
    }

    override fun onBackPressed() {

    }

    fun loadDefaultPreferences() {
        getPrefs(this).edit {
            putBoolean(Preferences.FIRST_LOAD, false)
            putInt(Preferences.MAIN_SCREEN_ICONS_COUNT, 5)
            putInt(Preferences.ALLAPPS_COLUMN_COUNT, 5)
            putInt(Preferences.ALLAPPS_ROW_COUNT, 7)
        }
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppShortcut) {
//            stages.currentItem = 1
            val newShortcut = AppShortcut(this, v.appInfo)
//            newShortcut.applyIcon(200,200)
            newShortcut.layoutParams = v.layoutParams
            newShortcut.width = v.width
            newShortcut.height = v.height
            newShortcut.setOnLongClickListener(newShortcut)
            val dragShadow = v.createDragShadow()
            v.startDrag(ClipData.newPlainText("",""), dragShadow, Pair(null, newShortcut), 0)
        }
        return true
    }
}
