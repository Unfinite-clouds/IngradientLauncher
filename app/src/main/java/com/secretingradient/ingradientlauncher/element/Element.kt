package com.secretingradient.ingradientlauncher.element

import android.view.View

class Element(var view: View) {

}

fun isElement(v: View?): Boolean {
    return v as? AppView ?: v as? FolderView ?: v as? WidgetView != null
}