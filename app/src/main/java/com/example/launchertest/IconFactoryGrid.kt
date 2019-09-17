package com.example.launchertest

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setMargins
import kotlin.math.min
import kotlin.random.Random

object IconFactoryGrid {

    fun createIcon(context: Context, appInfo: AppInfo, gridWidth: Int, gridHeight: Int, ncols: Int, nrows: Int, iconSizeDesired: Int = Int.MAX_VALUE): View {
        val iconViewWidth = gridWidth/ncols
        val iconViewHeight = gridHeight/nrows
//        val maxIconSize = (0.75*iconViewWidth).toInt()
//        val iconSize = if (iconSizeDesired < maxIconSize) iconSizeDesired else maxIconSize
        // max icon visible size = 75% from view size. Hence minimum padding will be 25%
        val iconSize = min(min(0.75f*iconViewWidth, 0.75f*iconViewHeight).toInt(), iconSizeDesired)

//        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//        params.width = iconViewWidth
        val layoutParams = GridLayout.LayoutParams()
        layoutParams.width = iconViewWidth
        layoutParams.height = iconViewWidth
        layoutParams.setMargins((iconViewWidth-iconSize)/2)
        layoutParams.setGravity(11)

        val tv = TextView(context)

        val view = ImageView(context).apply {
//            setImageDrawable(appInfo.icon)
            setOnClickListener {
                context.startActivity(context.packageManager.getLaunchIntentForPackage(appInfo.packageName))
            }
//            layoutParams = layoutParams
//            this.width = iconViewWidth
//            this.height = iconViewHeight
//            val paddingH = (iconViewWidth-iconSize)/2
//            val paddingV = (iconViewHeight-iconSize)/2
//            setPadding(paddingH, paddingV, paddingH, paddingV)
            setBackgroundDrawable(appInfo.icon)
        }
        view.layoutParams = layoutParams
        view.setImageDrawable(appInfo.icon)
        view.setBackgroundColor(Color.argb(100, Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)))
//        view.scaleX = iconSize/appInfo.icon.intrinsicWidth.toFloat()
//        view.scaleY = iconSize/appInfo.icon.intrinsicHeight.toFloat()
        println("${appInfo.icon.intrinsicWidth} ${view.width}")

        return view
    }
}