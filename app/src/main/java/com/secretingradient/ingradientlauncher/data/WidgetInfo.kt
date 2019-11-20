package com.secretingradient.ingradientlauncher.data

import android.content.Context
import com.secretingradient.ingradientlauncher.element.WidgetView

class WidgetInfo(val packageName: String, val snapW: Int, val snapH: Int /*dont need this*/) : Info {
    override fun createData(index: Int): WidgetData {
        return WidgetData(index, packageName, snapW, snapH)
    }

    override fun createView(context: Context): WidgetView {
        return WidgetView(context)
    }
}