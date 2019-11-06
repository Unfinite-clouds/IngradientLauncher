package com.secretingradient.ingradientlauncher

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.element.AppData

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_root)


//        DataKeeper.deleteFile(this, DataKeeper.ALL_APPS)
//        DataKeeper.deleteFile(this, DataKeeper.MAIN_STAGE_APPS)
//        DataKeeper.deleteFile(this, DataKeeper.USER_STAGE_APPS)

        DataKeeper.init(this)
        DataKeeper2.init(this)

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


        val apps = DataKeeper2.allAppsIds

        for (i in 0..10) {
            DataKeeper2.userStageData[i / 2 * 2] = AppData(apps[i])
        }
        DataKeeper2.dumpFile(this, DataKeeper2.userStageData, DataKeeper2.USER_STAGE_DATA)


        getPrefs(this).edit {putBoolean(Preferences.FIRST_LOAD, true)}
        if (getPrefs(this).getBoolean(Preferences.FIRST_LOAD, true))
            Preferences.loadDefaultPreferences(this)

        val root = findViewById<LauncherRootLayout>(R.id.launcher_root)
        root.initViewPager(findViewById(R.id.root_viewpager))

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

