package com.example.launchertest

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.research_layout.*

class ResearchActivity : AppCompatActivity() {

    lateinit var iter: Iterator<View>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.research_layout)
//        Storable.deleteFile(this, Storable.CUSTOM_GRID_APPS)
        AppManager.loadAllApps(this)

/*        val allApps = AppManager.allApps
        var i = 0
        for (app in allApps) {
            if (i > 12) break
            AppManager.applyCustomGridChanges(this, i, app.key)
            i++
        }*/
        layoutInflater.inflate(R.layout.research_merge, research_root, true)
    }
}
