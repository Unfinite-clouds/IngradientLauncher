package com.secretingradient.ingradientlauncher.data

class AppData(var position: Int, val appId: String) : Data {
    companion object {private const val serialVersionUID = 44001L}

    override fun createInfo(dataKeeper: DataKeeper): AppInfo {
        return dataKeeper.createAppInfo(appId)
    }
}
