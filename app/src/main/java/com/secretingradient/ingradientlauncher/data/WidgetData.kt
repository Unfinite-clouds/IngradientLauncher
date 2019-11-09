package com.secretingradient.ingradientlauncher.data

class WidgetData(var position: Int, val packageName: String, val snapW: Int, val snapH: Int) : Data {
    companion object {private const val serialVersionUID = 44003L}

    override fun createInfo(dataKeeper: DataKeeper): WidgetInfo {
        return WidgetInfo(packageName, snapW, snapH)
    }
}