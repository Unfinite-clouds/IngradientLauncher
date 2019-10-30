package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.view.View

class FolderInfo(val apps: MutableList<AppInfo>) : ElementInfo() {
    companion object {
        private const val serialVersionUID = 4402L
        fun createView(context: Context, apps: MutableList<String>): FolderView {
            return FolderView(context).apply { apps.addAll(0, apps) }
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