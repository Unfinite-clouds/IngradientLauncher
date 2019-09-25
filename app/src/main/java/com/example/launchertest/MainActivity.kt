package com.example.launchertest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.example.launchertest.launcher_skeleton.StageAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        AppManager.loadAllApps(this)

        if (getPrefs(this).getBoolean(Preferences.FIRST_LOAD, true))
            loadDefaultPreferences()

        val stages = findViewById<ViewPager2>(R.id.root_viewpager)
        stages.adapter = StageAdapter()
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
}
