package com.secretingradient.ingradientlauncher

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 */
class WidgetTest : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetId: Int, newOptions: Bundle?) {
/*        println("onAppWidgetOptionsChanged")
        newOptions?.let {
            println("${it.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)}, " +
                    "${it.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)}, " +
                    "${it.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)}, " +
                    "${it.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)}")
        }*/
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.widget_test)
    views.setTextViewText(R.id.appwidget_text, widgetText)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}