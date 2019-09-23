package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import com.example.launchertest.AppInfo
import com.example.launchertest.randomColor


class AppShortcut constructor(context: Context, attributeSet: AttributeSet?) : TextView(context, attributeSet) {
    var appInfo: AppInfo = AppInfo()
        set(value) {
            field = value
            text = field.label
            field.icon?.bounds = Rect(0,0,width,height)

        }
    val icon: Drawable?
        get() = compoundDrawables[1]

    constructor(context: Context, appInfo: AppInfo, attributeSet: AttributeSet? = null) : this(context, attributeSet) {
        this.appInfo = appInfo
    }

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        setTextColor(Color.WHITE)
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        includeFontPadding = false

        maxLines = 1
        setBackgroundColor(randomColor())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val iconSize = kotlin.math.min(w,(h-textSize).toInt())
        val x = 0
        val y = (h-textSize-iconSize).toInt()/2
        appInfo.icon?.bounds = Rect(x, y, x+iconSize, y+iconSize)
        setCompoundDrawables(null, appInfo.icon, null, null)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun rasterize(drawable: Drawable, bitmap: Bitmap, canvas: Canvas, size: Int) {
        drawable.setBounds(0, 0, size, size)
        canvas.setBitmap(bitmap)
        drawable.draw(canvas)
    }

}