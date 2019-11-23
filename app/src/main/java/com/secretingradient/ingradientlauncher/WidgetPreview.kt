package com.secretingradient.ingradientlauncher

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.widget.ImageView
import com.secretingradient.ingradientlauncher.data.WidgetPreviewInfo

class WidgetPreview : ImageView {
    val previewInfo: WidgetPreviewInfo
    val widgetInfo: AppWidgetProviderInfo
        get() = previewInfo.widgetInfo
    constructor(context: Context, previewInfo: WidgetPreviewInfo) : super(context) {
        this.previewInfo = previewInfo
        setImageDrawable(previewInfo.previewImage)
    }
}