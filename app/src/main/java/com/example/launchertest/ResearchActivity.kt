package com.example.launchertest

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
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


        test_btn.setOnClickListener {
            if (!this::iter.isInitialized)
                iter = (((test_vp.getChildAt(0) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0) as LauncherPageGrid).children.iterator()
            (iter.next() as DummyCell).shortcut = null
        }
    }
}
