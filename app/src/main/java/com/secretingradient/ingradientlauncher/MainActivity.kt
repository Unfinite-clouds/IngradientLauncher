package com.secretingradient.ingradientlauncher

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.data.DataKeeper
import com.secretingradient.ingradientlauncher.element.AppData

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_root)

//        val apps = DataKeeper.allApps
//
//        for (i in 0..12) {
//            DataKeeper.mainStageAppsData.add(apps[i]!!)
//        }
//        DataKeeper.dumpMainStageApps(this)
//
//        for (i in 0..12) {
//            DataKeeper.userStageAppsData[i/2*2] = apps[i]!!
//        }
//        DataKeeper.dumpUserStageApps(this)

//        DataKeeper.init(this)

        val dk = DataKeeper(this)
        val appIds = dk.allAppsIds

        // filling
        for (i in 0..10) {
            dk.userStageData.onInserted(i / 2 * 2, AppData(appIds[i]), true)
        }


        getPrefs(this).edit {putBoolean(Preferences.FIRST_LOAD, true)}
        if (getPrefs(this).getBoolean(Preferences.FIRST_LOAD, true))
            Preferences.loadDefaultPreferences(this)

        val launcher = findViewById<LauncherRootLayout>(R.id.launcher_root)
        launcher.initViewPager(findViewById(R.id.root_viewpager), dk)

    }

    override fun onBackPressed() {
        super.onBackPressed()  // do back stack
    }

    class PageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(view: View, position: Float) {
//        println("Transfroming: $relativePosition")
        }
    }
}

