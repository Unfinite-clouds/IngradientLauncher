package com.secretingradient.launchertest

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuPopupHelper
import com.secretingradient.launchertest.data.AppInfo
import kotlin.math.min


class AppView : TextView {
    var info: AppInfo? = null
        set(value) {
            field = value
            value?.let {
                text = value.label
                icon = value.icon
            }
        }

    var icon: Drawable?
        get() = compoundDrawables[1] // returns clone
        set(value) {
            val drawable = value?.constantState?.newDrawable(context.resources)
            drawable?.bounds = iconBounds
            setCompoundDrawables(null, drawable, null, null)
        }

    private var iconBounds = Rect()
    private var iconPaddingBottom = toPx(5).toInt()
    private var menuHelper: MenuPopupHelper? = null
    val snapWidth = 2
    val snapHeight = 2

    var desiredIconSize = Int.MAX_VALUE


    init {
//        layoutParams = SnapLayout.SnapLayoutParams(-1, snapWidth, snapHeight)
        layoutParams = ViewGroup.LayoutParams(150,150)
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        includeFontPadding = false
//        maxLines = 1
        setLines(1)
        setTextColor(Color.WHITE)
        ellipsize = TextUtils.TruncateAt.END
    }

    constructor(context: Context, info: AppInfo? = null) : super(context) {
        this.info = info
    }
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    private fun computeIconBounds(width: Int, height: Int) {
        val w = width - paddingLeft - paddingRight
        val h = height - paddingTop - paddingBottom - (textSize).toInt() - iconPaddingBottom
        val maxSize = min(w, h)
        val iconSize = if (desiredIconSize != 0) min(desiredIconSize, maxSize) else maxSize
        val x = 0
        val y = h - iconSize
        iconBounds = Rect(x, y, x+iconSize, y+iconSize)
    }

    private fun updateIconBounds() {
        icon?.bounds = iconBounds
        setCompoundDrawables(null, icon, null, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        computeIconBounds(w, h)
        updateIconBounds()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    companion object : OnClickListener {
        override fun onClick(v: View) {
            if (v is AppView)
                v.launchApp()
        }
    }

    fun launchApp() {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(info!!.packageName))
    }

    fun intentToInfo() {
        val uri = Uri.fromParts("package", info!!.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
//        return intent
    }

    fun intentToUninstall() {
        val uri = Uri.fromParts("package", info!!.packageName, null)
        val intent = Intent(Intent.ACTION_DELETE, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
//        return intent
    }

    private fun rasterize(drawable: Drawable, bitmap: Bitmap, canvas: Canvas, size: Int) {
        drawable.setBounds(0, 0, size, size)
        canvas.setBitmap(bitmap)
        drawable.draw(canvas)
    }

    override fun toString(): String {
        return "${this.hashCode().toString(16)} - ${info!!.label}, icon_bounds: ${icon?.bounds}, parent: $parent"
    }

}