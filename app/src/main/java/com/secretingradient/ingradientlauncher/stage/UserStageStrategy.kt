package com.secretingradient.ingradientlauncher.stage

import android.graphics.Color
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.Element
import com.secretingradient.ingradientlauncher.element.FolderView
import com.secretingradient.ingradientlauncher.element.WidgetView
import com.secretingradient.ingradientlauncher.sensor.BaseSensor

/* TODO:
    openFolder
    onXSelected (for show/hide sensors)
    isTouchOutsideFolder
 */

private abstract class UserStageStrategy(layout: LauncherRootLayout) : BaseStage(layout), View.OnTouchListener, View.OnLongClickListener {
    lateinit var currentSnapLayout: SnapLayout
    var shouldIntercept = false
    var folderPopup: PopupWindow? = null
    var inEditMode = false
    val sensorInfo = BaseSensor(context)
    val sensorRemove = BaseSensor(context)
    val sensorUninstall = BaseSensor(context)
    val sensorUp = BaseSensor(context)
    val sensorLeft = BaseSensor(context)
    val sensorRight = BaseSensor(context)
    var selectedView: View? = null
    var lastHoveredView: View? = null
    val touchPoint = Point()
    val reusablePoint = Point()
    val ghostView = ImageView(context).apply { setBackgroundColor(Color.LTGRAY); layoutParams = SnapLayout.SnapLayoutParams(-1,2,2) }


/*    enum class Action {
        CREATE_FOLDER, MOVE_ELEMENT, ON_SENSORED
    }*/

    fun preDispatch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            touchPoint.set(event.x.toInt(), event.y.toInt())
            selectedView = trySelect(findViewAt(touchPoint))

            if (isTouchOutsideFolder())
                closeFolder()
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (v !is StageRootLayout && !inEditMode)
            return false

        touchPoint.set(event.x.toInt(), event.y.toInt())

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                if (isTouchOutsideFolder())
                    closeFolder()
                val touchedView = findViewAt(touchPoint)
                selectedView = trySelect(touchedView)
                lastHoveredView = touchedView
            }

            MotionEvent.ACTION_MOVE -> {
                if (selectedView != null) {
                    val hoveredView = findViewAt(touchPoint, lastHoveredView)
                    if (hoveredView != lastHoveredView && hoveredView != null) {
                        if (lastHoveredView != null)
                            onExitHover(lastHoveredView!!)
                        onHover(selectedView!!, hoveredView, touchPoint)
                    }
                    lastHoveredView = hoveredView
                }
            }

            MotionEvent.ACTION_UP -> {
                if (selectedView != null) {
                    val hoveredView = findViewAt(touchPoint, lastHoveredView)
                    if (hoveredView != null) {
                        onPerformAction(selectedView!!, hoveredView, touchPoint)
                    }
                    lastHoveredView = hoveredView
                }
                if (isTapUp()) // not after longPress
                    endEditMode()
            }

        }



        return true
    }

    abstract fun isTapUp(): Boolean

    fun onExitHover(lastHoveredView: View) {
        when (lastHoveredView) {
            is BaseSensor -> lastHoveredView.onExitSensor()
            is FolderView -> previewCancelFolder(lastHoveredView)
        }
    }

    fun onHover(selectedView: View, hoveredView: View, touchPoint: Point) {
        when {
            selectedView is AppView && hoveredView is AppView -> previewCreateFolder(hoveredView)
            isElement(selectedView) && hoveredView is BaseSensor -> hoveredView.onSensored(selectedView)
            isElement(selectedView) && hoveredView is SnapLayout -> moveElement(ghostView, hoveredView, touchPoint)
        }
    }

    fun onPerformAction(selectedView: View, hoveredView: View, touchPoint: Point) {
        when {
            selectedView is AppView && hoveredView is FolderView -> hoveredView.addApps(selectedView)
            isElement(selectedView) && hoveredView is BaseSensor -> hoveredView.onPerformAction(selectedView)
            isElement(selectedView) && hoveredView is SnapLayout -> moveElement(selectedView, hoveredView, touchPoint)
        }
    }

    fun moveElement(element: View, snapLayout: SnapLayout, touchPoint: Point) {
        val pos = getTouchPositionOnSnap(snapLayout, touchPoint)
        snapLayout.moveView(element, pos)
    }

    fun previewCreateFolder(appView: AppView): FolderView {
        val folder = FolderView(context, appView)
        currentSnapLayout.removeView(appView)
        currentSnapLayout.addNewView(folder, (appView.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
        return folder
    }

    fun previewCancelFolder(lastHoveredView: FolderView) {
        currentSnapLayout.removeView(lastHoveredView)
        currentSnapLayout.addNewView(lastHoveredView[0], (lastHoveredView.layoutParams as SnapLayout.SnapLayoutParams).position, 2, 2)
        lastHoveredView.clear()
    }

    override fun onLongClick(v: View?): Boolean {
        closeFolder()
        startEditMode()
        return true
    }

    fun startEditMode() {
        inEditMode = true
        disallowVScroll()
        shouldIntercept = true
    }

    fun endEditMode() {
        inEditMode = false
        shouldIntercept = false
    }

    abstract fun removeElement(element: Element)

    abstract fun openFolder(folder: FolderView)

    fun closeFolder() {
        folderPopup?.dismiss()
        folderPopup = null
    }

    abstract fun isTouchOutsideFolder(): Boolean

    fun trySelect(v: View?): View? {
        if (!isElement(v))
            return null
        disallowHScroll()
        when (v) {
            is AppView -> onAppSelected(v)
            is FolderView -> onFolderSelected(v)
            is WidgetView -> onWidgetSelected(v)
        }
        return v
    }

    private fun getTouchPositionOnSnap(snapLayout: SnapLayout, touchPoint: Point): Int {
        toLocationInView(touchPoint, snapLayout, reusablePoint)
        return snapLayout.snapToGrid(reusablePoint, 2)
    }

    abstract fun onAppSelected(v: View)
    abstract fun onFolderSelected(v: View)
    abstract fun onWidgetSelected(v: View)

    abstract fun disallowHScroll(disallow: Boolean = true)

    fun isElement(v: View?): Boolean {
        return v as? AppView ?: v as? FolderView ?: v as? WidgetView != null
    }
}