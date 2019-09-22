package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ScaleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import com.example.launchertest.AppInfo


class AppShortcut private constructor(context: Context, attributeSet: AttributeSet?) : TextView(context, attributeSet) {
    lateinit var appInfo: AppInfo
    val iconDrawable: Drawable
        get() = compoundDrawables[1]
    private lateinit var scaleDrawable: ScaleDrawable

    constructor(context: Context, appInfo: AppInfo, attributeSet: AttributeSet? = null) : this(context, attributeSet) {
        this.appInfo = appInfo
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

//        scaleDrawable = ScaleDrawable(appInfo.icon, Gravity.CENTER, 0.5f,0.5f)
        setCompoundDrawables(null, appInfo.icon, null, null)
        iconDrawable.bounds.set(50, 50, 100, 100)
        println(iconDrawable.javaClass.simpleName)
        text = appInfo.label
        setTextColor(Color.WHITE)
        gravity = Gravity.BOTTOM
        includeFontPadding = false
        maxLines = 1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val myWidth = MeasureSpec.getSize(widthMeasureSpec)
        val myHeight = MeasureSpec.getSize(heightMeasureSpec)


        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}