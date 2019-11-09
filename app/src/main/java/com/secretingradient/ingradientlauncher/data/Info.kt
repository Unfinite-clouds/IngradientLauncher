package com.secretingradient.ingradientlauncher.data

import android.content.Context
import android.view.View

interface Info {
    fun createData(index: Int): Data
    fun createView(context: Context): View
}