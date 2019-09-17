package com.example.launchertest

import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout

class IconFactory(private val context: Context, nIconsOnScreen: Int) {
//    private val nIconsOnScreen = PreferenceManager(context).sharedPreferences.getInt(LauncherPreferences.MAIN_SCREEN_ICONS_COUNT, -1)
    private val nIconsOnScreen = nIconsOnScreen
    private val buttonWidth: Int
    private val iconWidth: Int
    private val screenWidth: Int

    init {
        val displaySize = Point()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(displaySize)
        screenWidth = displaySize.x
        buttonWidth = screenWidth/nIconsOnScreen
        val t = (0.75*buttonWidth).toInt()
        iconWidth = if (t > buttonWidth) buttonWidth else t
    }

    fun createIcon(appInfo: AppInfo): ImageView{
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.width = buttonWidth
//        params.leftMargin = buttonWidth-iconWidth
//        params.weight = 1f

        return ImageView(context).apply {
            setImageDrawable(appInfo.icon)
            setOnClickListener {
                context.startActivity(context.packageManager.getLaunchIntentForPackage(appInfo.packageName))
            }
            layoutParams = params
            val p = (buttonWidth-iconWidth)/2
            setPadding(p, p/2, p, p/2)
        }
    }
}