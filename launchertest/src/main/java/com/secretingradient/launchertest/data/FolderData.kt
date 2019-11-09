package com.secretingradient.launchertest.data

class FolderData(val position: Int, val appIds: List<String>) : Data {
    companion object {private const val serialVersionUID = 44001L}

    override fun createInfo(dataKeeper: DataKeeper): FolderInfo {
        return FolderInfo(MutableList(appIds.size) {
            dataKeeper.createAppInfo(appIds[it])
        })
    }
}