package com.secretingradient.ingradientlauncher.data

class AppData(var position: Int, val appId: String) : Data {
    companion object {private const val serialVersionUID = 44000L}

    override fun extract(dataKeeper: DataKeeper): AppState {
        return AppState(this, dataKeeper.createAppInfo(appId))
    }
}

