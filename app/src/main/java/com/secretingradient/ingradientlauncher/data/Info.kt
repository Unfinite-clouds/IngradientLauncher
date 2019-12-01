package com.secretingradient.ingradientlauncher.data

import android.content.Context
import android.view.View

interface Info {
    val snapWidth
        get() = 2
    val snapHeight
        get() = 2
    fun createData(index: Int): Data
    fun createView(context: Context, draggable: Boolean = false): View
}