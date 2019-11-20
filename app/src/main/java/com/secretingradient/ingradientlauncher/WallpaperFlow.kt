package com.secretingradient.ingradientlauncher

import android.app.WallpaperManager
import android.content.Context
import android.os.IBinder
import androidx.recyclerview.widget.RecyclerView

class WallpaperFlow(val context: Context, val windowToken: IBinder) {
//    private var maxOverscroll = 12
    private val wallpaper = WallpaperManager.getInstance(context)

    fun scroll(xOffset: Float, yOffset: Float) {
        wallpaper.setWallpaperOffsets(windowToken, xOffset, yOffset)
    }

    class RecyclerScroller(val launcherRootLayout: LauncherRootLayout) : RecyclerView.OnScrollListener() {
        var maxScroll = 0
        private var scrollX = 0
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            scrollX += dx
            val wallOffset = scrollX.toFloat()/maxScroll
            launcherRootLayout.wallpaper.scroll(wallOffset, 0f)
        }
    }
}