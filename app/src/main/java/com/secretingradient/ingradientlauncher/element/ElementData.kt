package com.secretingradient.ingradientlauncher.element

import java.io.Serializable

sealed class ElementData : Serializable

class AppData(val id: String) : ElementData() {
    companion object {private const val serialVersionUID = 44001L}
}

class FolderData(var ids: List<String>) : ElementData() {
    companion object {private const val serialVersionUID = 44002L}
}

class WidgetData() : ElementData() {
    companion object {private const val serialVersionUID = 44003L}
}