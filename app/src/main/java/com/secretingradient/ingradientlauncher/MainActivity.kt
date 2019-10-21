package com.secretingradient.ingradientlauncher

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.stage.StageAdapter

class MainActivity : AppCompatActivity() {
    lateinit var launcherViewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)


//        Storable.deleteFile(this, Storable.ALL_APPS)
//        Storable.deleteFile(this, Storable.MAIN_STAGE_APPS)
//        Storable.deleteFile(this, Storable.USER_STAGE_APPS)

        SaveManager.init(this)

//        val apps = SaveManager.apps.iterator()
//
//        for (i in 0..12) {
//            SaveManager.dumpUserStageApps(this, i, apps.next().key)
//        }
//
//        val list = mutableListOf<String>()
//        for (i in 0..12) {
//            list.add(apps.next().key)
//        }
//        SaveManager.dumpMainStageApps(this, list)


        getPrefs(this).edit {putBoolean(Preferences.FIRST_LOAD, true)}
        if (getPrefs(this).getBoolean(Preferences.FIRST_LOAD, true))
            Preferences.loadDefaultPreferences(this)

        launcherViewPager = findViewById<ViewPager2>(R.id.root_viewpager)
        launcherViewPager.adapter = StageAdapter(this)
        launcherViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
    }

    override fun onBackPressed() {
        super.onBackPressed()  // remove
    }

    class PageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(view: View, position: Float) {
//        println("Transfroming: $relativePosition")
        }
    }
}

