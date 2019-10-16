package com.secretingradient.ingradientlauncher.element

import com.secretingradient.ingradientlauncher.SnapLayout
import java.io.Serializable

data class SnapElementInfo(val appInfo: AppInfo, val snapLayoutInfo: SnapLayout.SnapLayoutInfo) : Serializable {
    companion object { private const val serialVersionUID = 4402L }
}