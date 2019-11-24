package com.secretingradient.ingradientlauncher

import android.appwidget.AppWidgetHost
import android.content.Context

const val WIDGET_HOST_ID = 9001

class WidgetHost(context: Context) : AppWidgetHost(context, WIDGET_HOST_ID) {
    override fun allocateAppWidgetId(): Int {
        val id = super.allocateAppWidgetId()
        // TODO: delete on release
        println("add widget $id: ${appWidgetIds?.contentToString()}")
        return id
    }

    override fun deleteAppWidgetId(appWidgetId: Int) {
        super.deleteAppWidgetId(appWidgetId)
        println("delete widget $appWidgetId: ${appWidgetIds?.contentToString()}")
    }
}