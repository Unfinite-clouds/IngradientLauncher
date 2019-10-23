package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.view.View
import java.io.Serializable

abstract class ElementInfo : Serializable {
    companion object { private const val serialVersionUID = 4400L }

    abstract val snapWidth: Int
    abstract val snapHeight: Int

    abstract fun createView(context: Context): View
}