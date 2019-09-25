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
import com.example.launchertest.randomColor


class AppShortcut : TextView {
    var appInfo: AppInfo
        set(value) {
            field = value
            text = field.label
            field.icon?.bounds = Rect(0,0,width,height)
        }

    val icon: Drawable?
        get() = compoundDrawables[1]

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        includeFontPadding = false
        maxLines = 1
        setBackgroundColor(randomColor())
        setTextColor(Color.WHITE)
    }

    constructor(context: Context, appInfo: AppInfo) : super(context) {
        this.appInfo = appInfo
    }
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        this.appInfo = AppInfo("test", "test")
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