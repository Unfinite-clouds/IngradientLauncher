package com.example.launchertest.launcher_skeleton

import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.launchertest.R
import com.example.launchertest.getAllAppsList
import kotlinx.android.synthetic.main.activity_try_adaptive_icons.*

class TryAdaptiveIcons : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_adaptive_icons)

        val allApps = getAllAppsList(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val adaptiveIcon = allApps[28].icon as AdaptiveIconDrawable
            try_icons_imageView.setImageDrawable(adaptiveIcon)

        } else {
            TODO("VERSION.SDK_INT < O")
        }
    }
}
