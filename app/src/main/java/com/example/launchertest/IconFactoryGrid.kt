package com.example.launchertest

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.view.iterator
import kotlin.math.min
import kotlin.random.Random


object IconFactoryGrid {

    fun createIcon(context: Context, appInfo: AppInfo, gridWidth: Int, gridHeight: Int, ncols: Int, nrows: Int, iconSizeDesired: Int = Int.MAX_VALUE): View {
        val iconViewWidth = gridWidth/ncols
        val iconViewHeight = gridHeight/nrows
        // max icon visible size = 75% from view size. Hence minimum margin will be 25%
        val iconSize = min(min(0.75f*iconViewWidth, 0.75f*iconViewHeight).toInt(), iconSizeDesired)
        val textSize = 42 // TODO - replace magic value

        val view = LayoutInflater.from(context).inflate(R.layout.shortcut_icon, null)
        view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> println("Layout: ${right-left}, ${bottom-top}") }

        val image = view.findViewById<ImageView>(R.id.shortcut_icon_image)
        image.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> println("image: ${right-left}, ${bottom-top}") }
        image.setImageDrawable(appInfo.icon)
        image.layoutParams.height = iconSize
        image.layoutParams.width = iconSize

        val label = view.findViewById<TextView>(R.id.shortcut_icon_label)
        label.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> println("label: ${right-left}, ${bottom-top}") }

        val layoutParams = GridLayout.LayoutParams()
        layoutParams.width = iconSize
        layoutParams.height = iconSize + textSize //label.textSize.toInt()*2
        layoutParams.setMargins((iconViewWidth-iconSize)/2, (iconViewHeight-iconSize-textSize)/2, (iconViewWidth-iconSize)/2, (iconViewHeight-iconSize-textSize)/2)
        layoutParams.setGravity(Gravity.CENTER)
        label.text = appInfo.label

//        println("$iconSize, ${label.textSize}") // 108, 28.0

        view.layoutParams = layoutParams
        view.setOnClickListener { context.startActivity(context.packageManager.getLaunchIntentForPackage(appInfo.packageName)) }
        view.setOnLongClickListener { createPopupMenu(it, context, appInfo)}
        view.setBackgroundColor(Color.argb(100, Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)))

        return view
    }

    private fun createPopupMenu(view: View, context: Context, appInfo: AppInfo): Boolean {
        val builder = MenuBuilder(view.context)
        val inflater = MenuInflater(view.context)
        inflater.inflate(R.menu.shortcut_popup_menu, builder)

        // set items
        for (item in builder.iterator()) {
            when (item.itemId) {
                R.id.popup_menu_info -> item.intent = intentAppSettings(appInfo.packageName)
                R.id.popup_menu_uninstall -> item.intent = intentAppDelete(appInfo.packageName)
            }
            item.setOnMenuItemClickListener { context.startActivity( it.intent ); return@setOnMenuItemClickListener true }
        }

        val menuHelper = MenuPopupHelper(view.context, builder, view)
        menuHelper.setForceShowIcon(true)
        menuHelper.show()
        return true
    }

    private fun intentAppSettings(packageName: String): Intent {
        val uri = Uri.fromParts("package", packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun intentAppDelete(packageName: String): Intent {
        val uri = Uri.fromParts("package", packageName, null)
        val intent = Intent(Intent.ACTION_DELETE, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
}