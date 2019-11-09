package com.secretingradient.ingradientlauncher.data

class FolderData(val position: Int, val appIds: List<String>) : Data {
    companion object {private const val serialVersionUID = 44002L}

    override fun createInfo(dataKeeper: DataKeeper): FolderInfo {
        return FolderInfo(MutableList(appIds.size) {
            dataKeeper.createAppInfo(appIds[it])
        })
    }
}