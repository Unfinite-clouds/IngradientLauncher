package com.secretingradient.ingradientlauncher.stage

import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.TrashView

class UserStage(launcherRootLayout: LauncherRootLayout) : BasePagerSnapStage(launcherRootLayout) {
    val FLIP_ZONE = toPx(40).toInt()

    var apps = DataKeeper.userStageAppsData
    var folders = DataKeeper.userStageFoldersData
    override var columnCount = getPrefs(context).getInt(Preferences.USER_STAGE_COLUMN_COUNT, -1)
    override var rowCount = getPrefs(context).getInt(Preferences.USER_STAGE_ROW_COUNT, -1)
    override var pageCount = getPrefs(context).getInt(Preferences.USER_STAGE_PAGE_COUNT, -1)
    var cellPadding = toPx(6).toInt()
    override val stageLayoutId = R.layout.stage_1_user
    override val viewPagerId = R.id.user_stage_pager
    override val pagerAdapter = PagerSnapAdapter(apps, folders)
    lateinit var trashView: TrashView
    val currentSnapLayout: SnapLayout
        get() = (stageViewPager.getChildAt(0) as ViewGroup).getChildAt(0) as SnapLayout

    private val editModeListener = EditModeListener()
    private val editModeDetector = GestureDetector(context, editModeListener)

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        trashView = stageRootLayout.findViewById(R.id.trash_view2)
        stageRootLayout.setOnTouchListener(this)
        stageRootLayout.shouldIntercept = true
//        trashView.setOnTouchListener(this)
//        stageViewPager.offscreenPageLimit = 2
//        (stageViewPager.getChildAt(0) as ViewGroup).clipChildren = false
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // todo remove
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            println("${MotionEvent.actionToString(event.action)} x = ${event.x} y = ${event.y} selected = ${editModeListener.selected?.javaClass?.simpleName}, v = ${v.javaClass.simpleName}")
        }

//        stageViewPager.getChildAt(0).onTouchEvent(event)
        editModeDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                editModeListener.onDown(v, event)
            }

            MotionEvent.ACTION_MOVE -> {
                editModeListener.onMove(v, event)
            }

            MotionEvent.ACTION_UP -> {
                editModeListener.onUp(v, event)
            }

            MotionEvent.ACTION_CANCEL -> {
                editModeListener.onCancel(v, event)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            println("${MotionEvent.actionToString(event.action)} x = ${event.x} y = ${event.y} selected = ${editModeListener.selected?.javaClass?.simpleName}, v = ${v.javaClass.simpleName}")
        }
        return true
    }

    inner class EditModeListener : GestureDetector.SimpleOnGestureListener() {
        var selected: AppView? = null
        private val touchPoint = Point()
        private val ghostView = ImageView(context).apply { setBackgroundColor(Color.LTGRAY); layoutParams = SnapLayout.SnapLayoutParams(-1,2,2) }
        private var inEditMode = false
        private val scaleInEditMode = 0.85f
        private val scaleSelected = 1.4f
        private val selectedPivot = Point()
        private val localPoint = Point()
        private var movePos = -1
        private var action = -1
        private val MOVE = 0
        private val INSERT = 1
        private val REMOVE = 2
        private val FOLDER = 3
        private var canEndEditMode = true

        override fun onShowPress(e: MotionEvent?) {
//            println("onShowPress $e")
            // it help us to know if this event for swipe <-> or for move app
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            if (inEditMode && selected == null && canEndEditMode) {
                endEditMode()
            }
            return true
        }

        // *stupid fucking android events*
        fun onDown(v: View, event: MotionEvent) {
            touchPoint.set(event.x.toInt(), event.y.toInt())
            stageViewPager.getChildAt(0).onTouchEvent(event)
            val hittedView = getHitView(touchPoint)

            if (inEditMode) {
                var hittedElement: AppView? = null
                if (hittedView is ViewPager2) {
                    hittedElement = getHitView(touchPoint, currentSnapLayout) as? AppView
                }
                select(hittedElement, touchPoint)  // save view that was selected for further drag
                disallowScrollStage()  // prevent launcherVP to be scrolled (up/down)
                canEndEditMode = true
                if (selected != null) {
                    movePos = -1
                    val lp = selected!!.layoutParams as SnapLayout.SnapLayoutParams
                    (ghostView.layoutParams as SnapLayout.SnapLayoutParams).set(-1, lp.snapWidth, lp.snapHeight)
                }
            }

        }

        private var lastHittedView: View? = null
        fun onMove(v: View, event: MotionEvent) {
            touchPoint.set(event.x.toInt(), event.y.toInt())

            if (selected == null || !inEditMode) {
                stageViewPager.getChildAt(0).onTouchEvent(event)  // scroll stageVP (left/right)
                return
            }

            val snap = currentSnapLayout
            var hittedView = getHitView(touchPoint) ?: stageRootLayout
            if (hittedView == stageViewPager) {
                hittedView = getHitView(touchPoint, snap) ?: snap
                if (hittedView == selected || hittedView == ghostView) hittedView = snap
            }
            var isNewMovePos = false
            if (hittedView == snap) {
                val newPos = getPosSnapped(touchPoint)
                if (newPos != movePos) {
                    movePos = newPos
                    isNewMovePos = true
                    (ghostView.layoutParams as SnapLayout.SnapLayoutParams).position = newPos
                }
            }

            if (hittedView != lastHittedView || isNewMovePos) {
                when (lastHittedView) {
                    // onExited
                    is TrashView -> {
                        (lastHittedView as TrashView).deactivate()
                    }
                    is AppView -> {
                        destroyFolder()
                        println("destroyFolder")
                    }
                    is SnapLayout -> {
                        snap.removeView(ghostView)
                    }
                }
                lastHittedView = hittedView
                when (hittedView) {
                    trashView -> {
                        action = REMOVE
                        trashView.activate()
                    }
                    is TrashView -> {
                        // flip UP
                        action = -1
                        snap.removeView(selected!!)
                        launcherRootLayout.stages[0].transferEvent(event, selected!!)
                        snap.removeView(ghostView)
                        endAction(true)
                        // todo: remove view from data
                    }
                    is SnapLayout -> {
                        action = MOVE
                        if (snap.canPlaceViewToPos(ghostView, movePos, selected)) {
                            // move ghostView
                            if (ghostView.parent == null) {
                                snap.addView(ghostView)
                            } else {
                                snap.moveView(ghostView, movePos)
                            }
                        }
                    }
                    is AppView -> {
                        action = FOLDER
                        createFolder()
                        println("createFolder")
                    }
                }
            }
        }

        fun onUp(v: View, event: MotionEvent) {
            stageViewPager.getChildAt(0).onTouchEvent(event)
            val snap = currentSnapLayout
            if (inEditMode && selected != null) {
                when (action) {
                    MOVE -> {
                        val oldPos = (selected!!.layoutParams as SnapLayout.SnapLayoutParams).position
                        snap.moveView(selected!!, movePos)
                        // update data
                        apps.remove(oldPos)
                        apps[movePos] = selected!!.appInfo.id
                        DataKeeper.dumpUserStageApps(context)
                    }
                    FOLDER -> {
                        // create folder at movePos
                    }
                    INSERT -> {

                    }
                    REMOVE -> {
                        snap.removeView(selected!!)
                        // todo: remove view from data
                    }
                }
            }

            snap.removeView(ghostView)
            endAction()
        }

        fun onCancel(v: View, event: MotionEvent) {

        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            startEditMode()
        }

        private fun createFolder() {

        }

        private fun destroyFolder() {

        }

        fun select(v: View?, touchPoint: Point) {
            selected = v as? AppView
            selected?.let {
                //        val dy = ( it.top - (it.parent as ViewGroup).height / 2 ) * (1f - scaleInEditMode)
                it.animate().scaleX(scaleSelected).scaleY(scaleSelected)/*.translationYBy(dy)*/.start()
                it.visibility = View.INVISIBLE
                it.animatorScale.start()
                selectedPivot.set(touchPoint.x, touchPoint.y)
                stageRootLayout.overlayView = it
                launcherRootLayout.dispatchToCurrent = true
            }
        }

        fun unselect() {
            selected?.let {
                it.animate().scaleX(1f).scaleY(1f).start()
                it.visibility = View.VISIBLE
            }
            selected = null
            stageRootLayout.overlayView = null
        }

        private fun endAction(dispatchToCurrent: Boolean = false) {
            unselect()
            lastHittedView = null
            trashView.deactivate()
            launcherRootLayout.dispatchToCurrent = dispatchToCurrent
            action = -1
        }

        private fun startEditMode() {
            inEditMode = true
            canEndEditMode = false
            disallowScrollStage()
            stageViewPager.animate().scaleX(scaleInEditMode).scaleY(scaleInEditMode).start()
        }

        private fun endEditMode() {
            stageViewPager.animate().scaleX(1f).scaleY(1f).start()
            inEditMode = false
        }

        private fun getPosSnapped(touchPoint: Point): Int {
            val snap = currentSnapLayout
            getLocationOnStage(snap, reusablePoint)
            localPoint.set(touchPoint.x - reusablePoint.x, touchPoint.y - reusablePoint.y)
            return snap.getPosSnapped(localPoint, 2)
        }
    }
}