package com.secretingradient.ingradientlauncher.data

import android.content.Context
import com.secretingradient.ingradientlauncher.element.FolderView
import com.secretingradient.ingradientlauncher.element.FolderViewDraggable

class FolderInfo(val apps: MutableList<AppInfo>) : Info {
    override fun createData(index: Int): FolderData {
        return FolderData(index, List(apps.size){
            apps[it].id
        })
    }

    override fun createView(context: Context, draggable: Boolean): FolderView {
        return if (!draggable)
            FolderView(context, apps)
        else
            FolderViewDraggable(context, apps)
    }
}