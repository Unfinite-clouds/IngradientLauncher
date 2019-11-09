package com.secretingradient.launchertest.data

class FolderInfo(val apps: MutableList<AppInfo>) : Info {
    override fun createData(index: Int): FolderData {
        return FolderData(index, List(apps.size){
            apps[it].id
        })
    }

}