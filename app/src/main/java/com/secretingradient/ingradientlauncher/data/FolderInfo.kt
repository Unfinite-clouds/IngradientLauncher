package com.secretingradient.ingradientlauncher.data

import android.content.Context
import android.view.View
import com.secretingradient.ingradientlauncher.element.FolderView

class FolderInfo(val apps: MutableList<AppInfo>) : Info {
    override fun createData(index: Int): FolderData {
        return FolderData(index, List(apps.size){
            apps[it].id
        })
    }

    override fun createView(context: Context): FolderView {
        return FolderView(context, apps)
    }
}