package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.view.View

class FolderInfo(val apps: MutableList<AppInfo>) : ElementInfo() {
    companion object {
        private const val serialVersionUID = 4402L
        fun createView(context: Context, apps: MutableList<String>): View {
            TODO("not implemented")
        }
    }

    override val snapWidth
        get() = 2
    override val snapHeight
        get() = 2

    override fun createView(context: Context): View {
        TODO("not implemented")
    }
}