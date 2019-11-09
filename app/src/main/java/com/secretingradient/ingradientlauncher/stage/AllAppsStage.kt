package com.secretingradient.ingradientlauncher.stage

import android.view.MotionEvent
import android.view.View
import com.secretingradient.ingradientlauncher.*
import kotlin.math.ceil

class AllAppsStage(launcherRootLayout: LauncherRootLayout) : BasePagerSnapStage(launcherRootLayout){
    override var rowCount = getPrefs(context).getInt(Preferences.ALLAPPS_STAGE_ROW_COUNT, -1)
    override var columnCount = getPrefs(context).getInt(Preferences.ALLAPPS_STAGE_COLUMN_COUNT, -1)
    override var pageCount = ceil(dataKeeper.allAppsIds.size.toFloat() / (rowCount*columnCount)).toInt()
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_2_all_apps
    override val viewPagerId = R.id.all_apps_vp
    override val pagerAdapter = PagerSnapAdapter()

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}