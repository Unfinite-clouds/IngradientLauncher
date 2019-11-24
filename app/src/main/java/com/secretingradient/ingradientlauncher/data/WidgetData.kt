package com.secretingradient.ingradientlauncher.data

class WidgetData(var position: Int, val providerId: String, val widgetId: Int, val snapW: Int, val snapH: Int) : Data {
    // name = "$packageName/$className". obtained from provider.flattenToString()
    companion object {private const val serialVersionUID = 44003L}

    override fun createInfo(dataKeeper: DataKeeper): WidgetInfo {
        return WidgetInfo(dataKeeper.getWidgetProviderInfo(providerId), widgetId, snapW, snapH)
    }
}