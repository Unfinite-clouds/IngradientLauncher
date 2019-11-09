package com.secretingradient.launchertest.data

class WidgetInfo(val packageName: String, val snapW: Int, val snapH: Int /*dont need this*/) : Info {
    override fun createData(index: Int): WidgetData {
        return WidgetData(index, packageName, snapW, snapH)
    }
}