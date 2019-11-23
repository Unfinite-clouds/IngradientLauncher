package com.secretingradient.ingradientlauncher.data

import android.appwidget.AppWidgetProviderInfo
import android.graphics.drawable.Drawable
import com.secretingradient.ingradientlauncher.getDensity
import com.secretingradient.ingradientlauncher.stage.ICON_SIZE
import kotlin.math.ceil

class WidgetPreviewInfo {
    var widgetInfo: AppWidgetProviderInfo
	var previewImage: Drawable?
	var icon: Drawable? = null
	var label: String
    var size: String

    constructor(widget: AppWidgetProviderInfo, dataKeeper: DataKeeper) {
        this.widgetInfo = widget
        val id = dataKeeper.getWidgetId(widget)

        // Icon
        // 1) try to find icon from appIcons in the same packageName
        for (entry in dataKeeper.iconDrawables) {
            if (entry.key.startsWith(widget.provider.packageName)) {
                icon = entry.value
                break
            }
        }
        if (icon == null)
        // 2) try to get icon from widget
            icon = dataKeeper.loadWidgetIcon(getDensity(dataKeeper.context), widget)
        icon?.setBounds(0, 0, ICON_SIZE, ICON_SIZE)

        previewImage = dataKeeper.widgetPreviewDrawables[id]  // can be null
        size = "${ceil(widget.minWidth/80f).toInt()} x ${ceil(widget.minHeight/80f).toInt()}"  // todo 80f
        label = widget.label
    }
}