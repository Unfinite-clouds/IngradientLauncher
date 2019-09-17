package com.example.launchertest

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

        val view = LayoutInflater.from(context).inflate(R.layout.shortcut_icon, null)
        view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> println("$left, $right, $top, $bottom") }
        val image = view.findViewById<ImageView>(R.id.shortcut_icon_image)
        val label = view.findViewById<TextView>(R.id.shortcut_icon_label)

        val layoutParams = GridLayout.LayoutParams()
        layoutParams.width = iconSize
        layoutParams.height = iconSize + label.textSize.toInt()*2
        layoutParams.setMargins((iconViewWidth-iconSize)/2, (iconViewHeight-iconSize)/2, (iconViewWidth-iconSize)/2, (iconViewHeight-iconSize)/2)
        layoutParams.setGravity(11)

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
            item.setOnMenuItemClickListener {
                menuItemClick(it, context)
            }
            item.intent = intentToAppSettings(appInfo.packageName)
        }

        val menuHelper = MenuPopupHelper(view.context, builder, view)
        menuHelper.setForceShowIcon(true)
        menuHelper.show()
        return true
    }

    private fun menuItemClick(menuItem: MenuItem, context: Context) : Boolean {
        when (menuItem.itemId) {
            R.id.popup_menu_info -> context.startActivity( menuItem.intent )
            R.id.popup_menu_uninstall -> Toast.makeText(context, "[redirect to uninstall]", Toast.LENGTH_LONG).show()
        }
        return true
    }

    private fun intentToAppSettings(packageName: String): Intent {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        return intent
    }
}