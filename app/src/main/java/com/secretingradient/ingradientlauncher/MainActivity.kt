package com.secretingradient.ingradientlauncher

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.data.DataKeeper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_root)

        val dk = DataKeeper(this)

        // filling
        for (i in 0 until 12) {
            dk.mainStageDataset.insert(i, dk.createAppInfo(i), false)
        }
        for (i in 0 until 24) {
            dk.userStageDataset.insert(i / 2 * 2, dk.createAppInfo(i), false)
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

