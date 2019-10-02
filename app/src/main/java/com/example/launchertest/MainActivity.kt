package com.example.launchertest

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    lateinit var launcherViewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

//        Storable.deleteFile(this, Storable.CUSTOM_GRID_APPS)
//        Storable.deleteFile(this, Storable.ALL_APPS)
        AppManager.loadAllApps(this)

        getPrefs(this).edit {putBoolean(Preferences.FIRST_LOAD, true)}
        if (getPrefs(this).getBoolean(Preferences.FIRST_LOAD, true))
            Preferences.loadDefaultPreferences(this)

/*
        val allApps = AppManager.allApps
        var i = 0
        for (app in allApps) {
            if (i > 12) break
            AppManager.applyCustomGridChanges(this, i, app.key)
            i++
        }
*/

        launcherViewPager = findViewById<ViewPager2>(R.id.root_viewpager)
        launcherViewPager.adapter = StageAdapter(this)
        launcherViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
    }

    override fun onBackPressed() {}

    class PageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(view: View, position: Float) {
//        println("Transfroming: $relativePosition")
        }
    }
}

