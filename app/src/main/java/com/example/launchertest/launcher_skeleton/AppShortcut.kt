package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.launchertest.AppInfo
import com.example.launchertest.R

class AppShortcut private constructor(context: Context, attributeSet: AttributeSet?) : LinearLayout(context, attributeSet) {

    constructor(context: Context, appInfo: AppInfo, attributeSet: AttributeSet? = null) : this(context, attributeSet) {
        this.appInfo = appInfo
        this.icon = appInfo.icon
        this.label = appInfo.label.toString()
        this.packageName = appInfo.packageName

        val layout = LayoutInflater.from(context).inflate(R.layout.screen_app_shortcut, this, true)
        iconView = layout.findViewById(R.id.app_shortcut_icon)
        labelView = layout.findViewById(R.id.app_shortcut_label)

        iconView.setImageDrawable(icon)
        labelView.text = label
    }

    lateinit var appInfo: AppInfo
    lateinit var icon: Drawable
    lateinit var label: String
    lateinit var packageName: String
    lateinit var iconView: ImageView
    lateinit var labelView: TextView

    init {
//        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        orientation = VERTICAL
    }
}