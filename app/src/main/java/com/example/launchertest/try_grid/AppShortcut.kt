package com.example.launchertest.try_grid

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.launchertest.AppInfo

class AppShortcut private constructor(context: Context, attributeSet: AttributeSet?) : LinearLayout(context, attributeSet) {

    constructor(context: Context, appInfo: AppInfo, attributeSet: AttributeSet? = null) : this(context, attributeSet) {
        this.appInfo = appInfo
        this.icon = appInfo.icon
        this.label = appInfo.label.toString()
        this.packageName = appInfo.packageName

//        val layout = LayoutInflater.from(context).inflate(R.layout.screen_app_shortcut, this, true)
//        iconView = layout.findViewById(R.id.app_shortcut_icon)
//        labelView = layout.findViewById(R.id.app_shortcut_label)

        iconView = ImageView(context)
        labelView = TextView(context)

        addView(iconView)
        addView(labelView)
    }

    lateinit var appInfo: AppInfo
    lateinit var icon: Drawable
    lateinit var label: String
    lateinit var packageName: String
    lateinit var iconView: ImageView
    lateinit var labelView: TextView





}