package com.secretingradient.ingradientlauncher.element

import android.appwidget.AppWidgetHostView
import android.content.Context
import com.secretingradient.ingradientlauncher.SnapLayout

class WidgetView(context: Context) : AppWidgetHostView(context) {
    var snapWidth: Int
        get() = (layoutParams as SnapLayout.SnapLayoutParams).snapWidth
        set(value) { (layoutParams as SnapLayout.SnapLayoutParams).snapWidth = value }
    var snapHeight: Int
        get() = (layoutParams as SnapLayout.SnapLayoutParams).snapHeight
        set(value) { (layoutParams as SnapLayout.SnapLayoutParams).snapHeight = value }

}