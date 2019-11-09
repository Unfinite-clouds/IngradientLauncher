package com.secretingradient.launchertest.data

import android.graphics.drawable.Drawable

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
}