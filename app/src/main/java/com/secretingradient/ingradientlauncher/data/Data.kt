package com.secretingradient.ingradientlauncher.data

import java.io.Serializable

interface Data : Serializable {
    fun createInfo(dataKeeper: DataKeeper) : Info
}