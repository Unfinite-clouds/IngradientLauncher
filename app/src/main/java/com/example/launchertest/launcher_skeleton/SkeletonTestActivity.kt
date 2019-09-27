package com.example.launchertest.launcher_skeleton

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.example.launchertest.AppManager
import com.example.launchertest.Preferences
import com.example.launchertest.R
import com.example.launchertest.getPrefs


class SkeletonTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        AppManager.loadAllApps(this)

        if (getPrefs(this).getBoolean(Preferences.FIRST_LOAD, true))
            loadDefaultPreferences()

        /*
        val allApps = AppManager.allApps
        var i = 0
        for (app in allApps) {
            if (i > 12) break
            AppManager.applyCustomGridChanges(this, app.key, i)
            i++
        }*/

        val stages = findViewById<ViewPager2>(R.id.root_viewpager)
        stages.adapter = StageAdapter()
        stages.orientation = ViewPager2.ORIENTATION_VERTICAL
        stages.setPageTransformer(PageTransformer())

        stages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
    }

    // Here is default preferences
    fun loadDefaultPreferences() {
        getPrefs(this).edit {
            putBoolean(Preferences.FIRST_LOAD, false)
            putInt(Preferences.MAIN_SCREEN_ICONS_COUNT, 5)
            putInt(Preferences.ALLAPPS_COLUMN_COUNT, 5)
            putInt(Preferences.ALLAPPS_ROW_COUNT, 7)
        }
    }
}


class PageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
//        println("Transfroming: $relativePosition")
    }
}