package com.example.launchertest

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.launchertest.launcher_skeleton.CustomGridAdapter
import com.example.launchertest.launcher_skeleton.DummyCell
import com.example.launchertest.launcher_skeleton.LauncherScreenGrid
import com.example.launchertest.launcher_skeleton.createCustomGrid
import kotlinx.android.synthetic.main.grid_test.*

class ResearchActivity : AppCompatActivity() {

    lateinit var iter: Iterator<View>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grid_test)
//        Storable.deleteFile(this, Storable.CUSTOM_GRID_APPS)
        AppManager.loadAllApps(this)

//        val allApps = AppManager.allApps
//        var i = 0
//        for (app in allApps) {
//            if (i > 12) break
//            AppManager.applyCustomGridChanges(this, i, app.key)
//            i++
//        }

        test_vp.adapter = object : CustomGridAdapter(this) {
            override fun createGrid(context: Context, page: Int): LauncherScreenGrid {
                return createCustomGrid(context, page)
            }
        }

        test_btn.setOnClickListener {
            if (!this::iter.isInitialized)
                iter = (((test_vp.getChildAt(0) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0) as LauncherScreenGrid).children.iterator()
            (iter.next() as DummyCell).shortcut = null
        }
    }
}
