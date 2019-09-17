package com.example.launchertest.launcher_skeleton

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.launchertest.R


class SeleketonTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleketon_test)

/*        val prefs = PreferenceManager(this).sharedPreferences
        prefs.edit(true) {
            putInt(LauncherPreferences.MAIN_SCREEN_ICONS_COUNT, 6)
            putInt(LauncherPreferences.ALLAPPS_COLUMNS_COUNT, 5)
            putInt(LauncherPreferences.ALLAPPS_ROWS_COUNT, 7)
        }*/

        val levelVP2 = findViewById<ViewPager2>(R.id.root_viewpager)
        levelVP2.adapter = LevelViewPager2Adapter()
        levelVP2.setPageTransformer(PageTransformer())
        levelVP2.orientation = ViewPager2.ORIENTATION_VERTICAL

        levelVP2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
    }
}


class PageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
//        println("Transfroming: $position")
    }
}