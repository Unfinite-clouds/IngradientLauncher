package com.secretingradient.ingradientlauncher.element

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.widget.FrameLayout
import com.secretingradient.ingradientlauncher.LauncherActivity
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.data.WidgetInfo

class WidgetView(context: Context, widgetInfo: WidgetInfo) : FrameLayout(context) {
    var snapWidth: Int
        get() = (layoutParams as SnapLayout.SnapLayoutParams).snapWidth
        set(value) { (layoutParams as SnapLayout.SnapLayoutParams).snapWidth = value }
    var snapHeight: Int
        get() = (layoutParams as SnapLayout.SnapLayoutParams).snapHeight
        set(value) { (layoutParams as SnapLayout.SnapLayoutParams).snapHeight = value }
    var widgetInfo: WidgetInfo = widgetInfo
        set(value) {
            field = value
            snapWidth = value.snapWidth
            snapHeight = value.snapHeight
        }

    val widget: AppWidgetHostView
        get() = getChildAt(0) as AppWidgetHostView

    init {
        layoutParams = SnapLayout.SnapLayoutParams(-1, widgetInfo.snapWidth, widgetInfo.snapHeight)
        addView((context as LauncherActivity).widgetHost.createView(context, widgetInfo.widgetId, widgetInfo.widgetProviderInfo))
        this.widgetInfo = widgetInfo
    }

}