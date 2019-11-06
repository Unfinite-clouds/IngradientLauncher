package com.secretingradient.ingradientlauncher.element

import android.view.View

class Element(var view: View) {
    var position = -1
    var snapWidth = -1
    var snapHeight = -1
}

fun isElement(v: View?): Boolean {
    return v as? AppView ?: v as? FolderView ?: v as? WidgetView != null
}