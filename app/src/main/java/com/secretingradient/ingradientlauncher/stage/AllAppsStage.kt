package com.secretingradient.ingradientlauncher.stage

import android.content.ClipData
import android.content.Context
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.view.setPadding
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.element.AppInfo
import com.secretingradient.ingradientlauncher.element.AppView
import kotlin.math.ceil

class AllAppsStage(context: Context) : BasePagerSnapStage(context), View.OnLongClickListener {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var apps = SaveManager.allAppsSorted
    var rowCount = getPrefs(context).getInt(Preferences.ALLAPPS_ROW_COUNT, -1)
    var columnCount = getPrefs(context).getInt(Preferences.ALLAPPS_COLUMN_COUNT, -1)
    var pageCount = ceil(apps.size.toFloat() / (rowCount*columnCount)).toInt()
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_2_all_apps
    override val viewPagerId = R.id.all_apps_vp
    override val pagerAdapter = AllAppsAdapter(context) as PagerSnapAdapter

    override fun inflateAndAttach(stageRoot: StageRoot) {
        super.inflateAndAttach(stageRoot)
        stageRoot.setOnDragListener(this)
    }

    inner class AllAppsAdapter(context: Context) : PagerSnapAdapter(context, columnCount, rowCount) {
        override fun getItemCount() = pageCount

        override fun createPage(page: Int): SnapLayout {
            val grid = SnapLayout(context, columnCount, rowCount)

            var position: Int
/*            for (i in 0 until grid.size) {
                position = i+grid.size*page
                if (position > apps.size - 1)
                    break

                val appInfo = SaveManager.getApp(apps[position])
                if (appInfo != null)
                    grid.putApp(createAppShortcut(appInfo), position)
            }*/
            return grid
        }

    }

    fun createAppShortcut(appInfo: AppInfo): AppView {
        return AppView(context, appInfo).apply { adaptApp(this) }
    }

    override fun adaptApp(app: AppView) {
        app.setOnLongClickListener(this@AllAppsStage)
        app.setPadding(cellPadding)
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is AppView) {
            v.showMenu()
            startDrag(v)
        }
        return true
    }

    private var dragApp: AppView? = null
    private var isFirstDrag = true

    override fun startDrag(v: View) {
        if (v is AppView) {
            val vcopy = AppView(context, v.appInfo)
            dragApp = v
            v.startDrag(ClipData.newPlainText("",""), v.createDragShadow(), DragState(vcopy, this), 0)
        }
    }

    override fun onFocus(event: DragEvent) {
        isFirstDrag = true
    }

    override fun onFocusLost(event: DragEvent) {
    }

    override fun onDragEnded(event: DragEvent) {
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        super.onDrag(v, event)

        when (event?.action) {

            DragEvent.ACTION_DRAG_STARTED -> {}

            DragEvent.ACTION_DRAG_ENTERED -> {}

            DragEvent.ACTION_DRAG_LOCATION -> {
                if (isFirstDrag) isFirstDrag = false else {
                    dragApp?.dismissMenu()
                    flipToStage(1, event)
                }
            }

            DragEvent.ACTION_DRAG_EXITED -> {}

            DragEvent.ACTION_DROP -> {}

            DragEvent.ACTION_DRAG_ENDED -> {}

            }

        return true
    }

}