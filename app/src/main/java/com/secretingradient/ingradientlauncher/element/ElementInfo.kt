package com.secretingradient.ingradientlauncher.element

import java.io.Serializable

abstract class ElementInfo : Serializable {
    companion object { private const val serialVersionUID = 4400L }

    abstract val snapWidth: Int
    abstract val snapHeight: Int
}