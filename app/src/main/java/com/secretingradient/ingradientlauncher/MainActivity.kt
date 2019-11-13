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
        fillStages(dk)

        getPrefs(this).edit {putBoolean(Preferences.FIRST_LOAD, true)}
        if (getPrefs(this).getBoolean(Preferences.FIRST_LOAD, true))
            Preferences.loadDefaultPreferences(this)

        val launcher = findViewById<LauncherRootLayout>(R.id.launcher_root)
        launcher.initViewPager(findViewById(R.id.root_viewpager), dk)

    }

    fun fillStages(dk: DataKeeper) {
        dk.mainStageDataset.deleteFile()
        for (i in 0 until 12) {
            dk.mainStageDataset.add(i, dk.createAppInfo(i))
        }

//        dk.userStageDataset.deleteFile()
//        for (i in 0 until 24) {
//            val v = i / 2 * 2 + i/14*14
//            dk.userStageDataset.put(v, dk.createAppInfo(i), true)
//        }
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

