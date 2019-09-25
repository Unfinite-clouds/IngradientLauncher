package com.example.launchertest.launcher_skeleton

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.example.launchertest.PreferenceKeys
import com.example.launchertest.R
import com.example.launchertest.getPrefs


class SkeletonTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skeleton_test)

        if (getPrefs(this).getBoolean(PreferenceKeys.LOAD_DEFAULTS, true))
            loadDefaultPreferences()

        val stages = findViewById<ViewPager2>(R.id.root_viewpager)
        stages.adapter = LevelViewPager2Adapter()
        stages.setPageTransformer(PageTransformer())
        stages.orientation = ViewPager2.ORIENTATION_VERTICAL

        stages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
    }

    // Here is default preferences
    fun loadDefaultPreferences() {
        getPrefs(this).edit {
            putBoolean(PreferenceKeys.LOAD_DEFAULTS, false)
            putInt(PreferenceKeys.MAIN_SCREEN_ICONS_COUNT, 5)
            putInt(PreferenceKeys.ALLAPPS_COLUMN_COUNT, 5)
            putInt(PreferenceKeys.ALLAPPS_ROW_COUNT, 7)
        }
    }
}


class PageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
//        println("Transfroming: $position")
    }
}