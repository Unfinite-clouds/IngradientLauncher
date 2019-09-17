package com.example.launchertest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.launchertest.launcher_skeleton.ScreenViewPager2Adapter
import kotlinx.android.synthetic.main.activity_view_pager_test.*

class ViewPagerTest : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager_test)

        vp_test.adapter = ScreenViewPager2Adapter()
//        vp_test.offscreenPageLimit = 5
    }
}
