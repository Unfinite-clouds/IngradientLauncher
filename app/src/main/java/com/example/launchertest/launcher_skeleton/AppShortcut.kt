package com.example.launchertest.launcher_skeleton

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import android.view.Gravity
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.view.iterator
import com.example.launchertest.R


class AppShortcut : TextView, View.OnLongClickListener, MenuItem.OnMenuItemClickListener, View.OnClickListener {
companion object {
    const val DISMISS_RADIUS = 20
}

    var appInfo: AppInfo
        set(value) {
            field = value
            text = field.label
            field.icon?.bounds = Rect(0,0,width,height)
        }

    val icon: Drawable?
        get() = compoundDrawables[1]

    var menuHelper: MenuPopupHelper? = null
    var goingToRemove = false

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        includeFontPadding = false
        maxLines = 1
        setTextColor(Color.WHITE)
        setOnClickListener(this)
        setOnLongClickListener(this)
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

    override fun onClick(v: View?) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(appInfo.packageName))
    }

    override fun onLongClick(view: View): Boolean {
        createPopupMenu(view)
        startDrag(view as AppShortcut)
        return true
    }

    private fun startDrag(shortcut: AppShortcut) {
//        shortcut.visibility = View.INVISIBLE
        shortcut.icon?.setColorFilter(Color.rgb(181, 232, 255), PorterDuff.Mode.MULTIPLY)

        val cell = (shortcut.parent as DummyCell)

        val data = ClipData.newPlainText("", "")
        val shadowBuilder = View.DragShadowBuilder(shortcut)
        shortcut.startDrag(data, shadowBuilder, Pair(cell, shortcut), 0)
        (shortcut.parent as DummyCell).removeAllViews()
    }

    private fun createPopupMenu(view: View) {
        val builder = MenuBuilder(view.context)
        val inflater = MenuInflater(view.context)
        inflater.inflate(R.menu.shortcut_popup_menu, builder)
        for (item in builder.iterator()) {
            item.setOnMenuItemClickListener(this)
        }
        menuHelper = MenuPopupHelper(view.context, builder, view)
        menuHelper?.setForceShowIcon(true)
        menuHelper?.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.popup_menu_info -> context.startActivity(intentToInfo())
            R.id.popup_menu_uninstall -> context.startActivity(intentToUninstall())
        }
        return true
    }

    private fun intentToInfo(): Intent {
        val uri = Uri.fromParts("package", appInfo.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun intentToUninstall(): Intent {
        val uri = Uri.fromParts("package", appInfo.packageName, null)
        val intent = Intent(Intent.ACTION_DELETE, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun rasterize(drawable: Drawable, bitmap: Bitmap, canvas: Canvas, size: Int) {
        drawable.setBounds(0, 0, size, size)
        canvas.setBitmap(bitmap)
        drawable.draw(canvas)
    }
}