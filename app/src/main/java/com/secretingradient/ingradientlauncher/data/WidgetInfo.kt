package com.secretingradient.ingradientlauncher.data

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import com.secretingradient.ingradientlauncher.element.WidgetView

class WidgetInfo(val widgetProviderInfo: AppWidgetProviderInfo, val widgetId: Int, override val snapWidth: Int, override val snapHeight: Int) : Info {
    override fun createData(index: Int): WidgetData {
        return WidgetData(index, widgetProviderInfo.provider.flattenToString(), widgetId, snapWidth, snapHeight)
    }

    override fun createView(context: Context): WidgetView {
        return WidgetView(context, this)
    }
}