package com.secretingradient.ingradientlauncher.data

import android.content.Context
import android.graphics.drawable.Drawable
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.AppViewDraggable

class AppInfo(
    val packageName: String,
    val name: String,
    val label: String,
    val icon: Drawable
) : Info {
    val id: String
        get() = "${packageName}_$name"

    override fun createData(index: Int): AppData {
        return AppData(index, id)
    }

    override fun createView(context: Context, draggable: Boolean): AppView {
        return if (!draggable)
            AppView(context, this)
        else
            AppViewDraggable(context, this)
    }
}