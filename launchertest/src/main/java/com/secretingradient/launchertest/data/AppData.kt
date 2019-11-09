package com.secretingradient.launchertest.data

import com.secretingradient.launchertest.AppView

class AppData(var position: Int, val appId: String) : Data {
    companion object {private const val serialVersionUID = 44000L}

    override fun extract(dataKeeper: DataKeeper): AppView.AppInfo {
        return dataKeeper.createAppInfo(appId)
    }
}
