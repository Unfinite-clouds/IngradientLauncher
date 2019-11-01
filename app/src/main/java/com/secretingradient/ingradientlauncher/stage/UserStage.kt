package com.secretingradient.ingradientlauncher.stage

import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.FolderView
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
        get() = stageRV.getChildAt(0) as SnapLayout

//    private val editModeListener = EditModeListener()
//    private val editModeDetector = GestureDetector(context, editModeListener)
    private val touchListener = UserStageOnTouch()

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        trashView = stageRootLayout.findViewById(R.id.trash_view2)
        stageRootLayout.setOnTouchListener(touchListener)
        stageRootLayout.shouldIntercept = true
//        trashView.setOnTouchListener(this)
//        stageVP.offscreenPageLimit = 2
//        (stageVP.getChildAt(0) as ViewGroup).clipChildren = false
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TODO("remove this") //To change body of created functions use File | Settings | File Templates.
    }

    private inner class UserStageOnTouch: View.OnTouchListener {
        private val longListener = View.OnLongClickListener { startEditMode(); true }
        val gestureRecognizer = GestureRecognizer(context, longListener).apply {  }
        var selected: View? = null
            set(value) { field = value; if (value == null)
            println("oops")}
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
        private val CREATE_FOLDER = 3
        private val INSERT_IN_FOLDER = 4
        private val REMOVE_FROM_FOLDER = 5
        private var canEndEditMode = true
        private var lastHittedView: View? = null

        /* stupid fucking android touch events -_- */
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val gesture = gestureRecognizer.recognizeTouchEvent(event)

            // todo remove
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                println("${MotionEvent.actionToString(event.action)}, $gesture, x = ${event.x} y = ${event.y} selected = ${selected?.javaClass?.simpleName}, v = ${v.javaClass.simpleName}")
            }

            val snap = currentSnapLayout
            touchPoint.set(event.x.toInt(), event.y.toInt())
            val hittedViewOnStage = getHitView(touchPoint, stageRootLayout, lastHittedView)
            var hittedView = if (hittedViewOnStage == stageVP) getHitView(touchPoint, currentSnapLayout, lastHittedView) ?: snap else hittedViewOnStage
            if (hittedView != null && (hittedView == selected || hittedView == ghostView))
                hittedView = snap

            if (!inEditMode) {
                // EDIT MODE OFF
                when {
                    gesture == Gesture.DOWN -> stageRV.onTouchEvent(event)

                    gesture == Gesture.TAP -> hittedView?.performClick().also { println("click performed at ${hittedView?.javaClass?.simpleName}") }

//                    gesture == Gesture.LONG_PRESS -> startEditMode()

                    gesture == Gesture.SCROLL_X -> { disallowScrollStage(); stageRV.onTouchEvent(event) }  // scroll stageVP (left/right)
                    gesture.isUp() -> stageRV.onTouchEvent(event)

                }
            } else {
                // EDIT MODE ON
                when {
                    gesture == Gesture.DOWN -> {
                        select(hittedView, touchPoint)
                        disallowScrollStage()  // prevent launcherVP to be scrolled (up/down)
                        canEndEditMode = true
                        if (selected != null) {
                            movePos = -1
                            val lp = selected!!.layoutParams as SnapLayout.SnapLayoutParams
                            (ghostView.layoutParams as SnapLayout.SnapLayoutParams).set(lp)
                        } else {
                            stageRV.onTouchEvent(event)
                        }
                    }

                    gesture == Gesture.TAP -> endEditMode()

                    gesture.isMove() && selected != null -> {
                        var isNewMovePos = false
                        if (hittedViewOnStage == stageVP) {
                            val newPos = getPosSnapped(snap, touchPoint)
                            if (newPos != movePos) {
                                movePos = newPos
                                isNewMovePos = true
                            }
                            if (hittedView == selected || hittedView == ghostView)
                                hittedView = snap
                        }

                        if (hittedView != lastHittedView || isNewMovePos) {

                            when (lastHittedView) {
                                // onExited
                                is TrashView -> {
                                    (lastHittedView as TrashView).deactivate()
                                }
                                is SnapLayout -> {
                                    if (hittedView !is SnapLayout)
                                        snap.removeView(ghostView)
                                }
                                is FolderView -> {
                                    val folder = lastHittedView as FolderView
                                    if (folder.folderSize == 1) {
                                        // revert preview folder
                                        snap.removeView(folder)
                                        snap.addNewView(folder[0], folder.layoutParams as SnapLayout.SnapLayoutParams)
                                        folder.clear()
                                    }
                                }
                            }

                            when (hittedView) {
                                trashView -> {
                                    action = REMOVE
                                    trashView.activate()
                                }
                                is TrashView -> {
                                    // flip UP
                                    if (selected is AppView) {
                                        action = -1
                                        snap.removeView(selected!!)
                                        launcherRootLayout.stages[0].transferEvent(event, selected!! as AppView)
                                        endAction(true)
                                        // todo: data update
                                    }
                                }
                                is SnapLayout -> {
                                    if (snap.canMoveViewToPos(ghostView, movePos, selected)) {
                                        action = MOVE
                                        // move ghostView
                                        if (ghostView.parent == null) {
                                            (ghostView.layoutParams as SnapLayout.SnapLayoutParams).position = movePos
                                            snap.addView(ghostView)
                                        } else {
                                            snap.moveView(ghostView, movePos)
                                        }
                                    }
                                }
                                is AppView -> {
                                    if (selected is AppView) {
                                        action = CREATE_FOLDER
                                        // preview only - don't create folder in data
                                        snap.removeView(hittedView)
                                        hittedView = FolderView(context, hittedView)
                                        snap.addNewView(hittedView, movePos, 2, 2)
                                    }
                                }
                                is FolderView -> {
                                    if (selected is AppView) {
                                        action = INSERT_IN_FOLDER
                                    }
                                }
                                else -> { action = -1 }
                            }
                        }
                    }

                    gesture.isUp() -> {
                        // UP
                        stageRV.onTouchEvent(event)

                        when (action) {
                            MOVE -> {
                                val oldPos = (selected!!.layoutParams as SnapLayout.SnapLayoutParams).position
                                snap.moveView(selected!!, movePos)
                                // update data
                                if (selected is AppView) {
                                    apps.remove(oldPos)
                                    apps[movePos] = (selected as AppView).appInfo.id
                                    DataKeeper.dumpUserStageApps(context)
                                }
                                // todo: data update
                            }
                            CREATE_FOLDER -> {
                                snap.removeView(selected!!)
                                (lastHittedView as FolderView).addApps(selected as AppView)
//                        save folder and apps (1 and selected removed)
                                // todo: data update
                            }
                            INSERT -> {

                            }
                            REMOVE -> {
                                snap.removeView(selected!!)
                                // todo: data update
                            }
                            INSERT_IN_FOLDER -> {
                                snap.removeView(selected!!)
                                (lastHittedView as FolderView).addApps(selected as AppView)
                                // todo: data update
                            }
                        }
                        snap.removeView(ghostView)
                        endAction()
                    }
                }
            }
//            println("hitted= ${hittedView?.javaClass?.simpleName}, selected= ${selected?.javaClass?.simpleName}")
//            println(" ")

            lastHittedView = hittedView

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            println("${MotionEvent.actionToString(event.action)} x = ${event.x} y = ${event.y} selected = ${editModeListener.selected?.javaClass?.simpleName}, v = ${v.javaClass.simpleName}")
            }
            return true
        }

        private fun select(v: View?, touchPoint: Point): View? {
            selected = v as? AppView ?: v as? FolderView
            selected?.let {
                //        val dy = ( it.top - (it.parent as ViewGroup).height / 2 ) * (1f - scaleInEditMode)
                it.animate().scaleX(scaleSelected).scaleY(scaleSelected)/*.translationYBy(dy)*/.start()
                it.visibility = View.INVISIBLE
                (it as? AppView)?.animatorScale?.start()
                selectedPivot.set(touchPoint.x, touchPoint.y)
                stageRootLayout.overlayView = it
                launcherRootLayout.dispatchToCurrent = true
            }
            return selected
        }

        private fun unselect() {
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
            stageVP.animate().scaleX(scaleInEditMode).scaleY(scaleInEditMode).start()
        }

        private fun endEditMode() {
            stageVP.animate().scaleX(1f).scaleY(1f).start()
            inEditMode = false
        }

        private fun getPosSnapped(snap: SnapLayout, touchPoint: Point): Int {
            getLocationOnStage(snap, reusablePoint)
            localPoint.set(touchPoint.x - reusablePoint.x, touchPoint.y - reusablePoint.y)
            return snap.getPosSnapped(localPoint, 2)
        }

    }


/*
    inner class EditModeListener : GestureDetector.SimpleOnGestureListener() {
        var selected: View? = null
        private val touchPoint = Point()
        private val ghostView = ImageView(context).apply { setBackgroundColor(Color.LTGRAY); layoutParams = SnapLayout.SnapLayoutParams(-1,2,2) }
        private var inEditMode = false
        private val scaleInEditMode = 0.85f
        private val scaleSelected = 1.4f
        private val selectedPivot = Point()
        private val localPoint = Point()
        private var lastHittedView: View? = null
        private var movePos = -1
        private var action = -1
        private val MOVE = 0
        private val INSERT = 1
        private val REMOVE = 2
        private val CREATE_FOLDER = 3
        private val INSERT_IN_FOLDER = 4
        private val REMOVE_FROM_FOLDER = 5
        private var canEndEditMode = true
        private var hittedFolder: FolderView? = null

        override fun onShowPress(e: MotionEvent?) {
//            println("onShowPress $e")
            // it help us to know if this event for swipe <-> or for move app
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            if (inEditMode && selected == null && canEndEditMode) {
                endEditMode()
            }
            if (!inEditMode && selected != null) {
                selected!!.performClick()
            }
            return true
        }

        // *stupid fucking android events*
        fun onDown(v: View, event: MotionEvent) {
            touchPoint.set(event.x.toInt(), event.y.toInt())
            stageVP.getChildAt(0).onTouchEvent(event)

            val hittedView = getHitView(touchPoint)

            var hittedView: View? = null
            if (hittedView is ViewPager2) {
                hittedView = getHitView(touchPoint, currentSnapLayout)
            }
            select(hittedView, touchPoint)  // save view that was selected for further drag

            if (inEditMode) {
                disallowScrollStage()  // prevent launcherVP to be scrolled (up/down)
                canEndEditMode = true
                if (selected != null) {
                    movePos = -1
                    val lp = selected!!.layoutParams as SnapLayout.SnapLayoutParams
                    (ghostView.layoutParams as SnapLayout.SnapLayoutParams).set(lp)
                }
            }

        }

        // bad performance - heavy calls of onMeasure and onLayout
        // TODO: Не проще ли разнести логику в конкретные View в OnTouchEvent?
        fun onMove(v: View, event: MotionEvent) {
            touchPoint.set(event.x.toInt(), event.y.toInt())

            if (selected == null || !inEditMode) {
                stageVP.getChildAt(0).onTouchEvent(event)  // scroll stageVP (left/right)
                return
            }

            val snap = currentSnapLayout
            var hittedView = getHitView(touchPoint) ?: stageRootLayout
            var isNewMovePos = false
            if (hittedView == stageVP) {
                val newPos = getPosSnapped(snap, touchPoint)
                if (newPos != movePos) {
                    movePos = newPos
                    isNewMovePos = true
                }

                hittedView = getHitView(touchPoint, snap) ?: snap
                if (hittedView == selected || hittedView == ghostView)
                    hittedView = snap
            }

//            println("hit = ${hittedView.javaClass.simpleName} last = ${lastHittedView?.javaClass?.simpleName} newPos = $isNewMovePos")

            if (hittedView != lastHittedView || isNewMovePos) {
                when (lastHittedView) {
                    // onExited
                    is TrashView -> {
                        (lastHittedView as TrashView).deactivate()
                    }
                    is SnapLayout -> {
                        if (hittedView !is SnapLayout)
                            snap.removeView(ghostView)
                    }
                    is FolderView -> {
                        if (hittedFolder!!.folderSize == 1) {
                            // preview only
                            snap.removeView(hittedFolder)
                            snap.addNewView(hittedFolder!![0], hittedFolder!!.layoutParams as SnapLayout.SnapLayoutParams)
                            hittedFolder!!.clear()
                            hittedFolder = null
                        }
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
                        if (selected is AppView) {
                            action = -1
                            snap.removeView(selected!!)
                            launcherRootLayout.stages[0].transferEvent(event, selected!! as AppView)
                            endAction(true)
                            // todo: data update
                        }
                    }
                    is SnapLayout -> {
                        if (snap.canMoveViewToPos(ghostView, movePos, selected)) {
                            action = MOVE
                            // move ghostView
                            if (ghostView.parent == null) {
                                (ghostView.layoutParams as SnapLayout.SnapLayoutParams).position = movePos
                                snap.addView(ghostView)
                            } else {
                                snap.moveView(ghostView, movePos)
                            }
                        }
                    }
                    is AppView -> {
                        if (selected is AppView) {
                            action = CREATE_FOLDER
                            // here preview only
                            snap.removeView(hittedView)
                            hittedFolder = FolderView(context)
                            hittedFolder!!.addApps(hittedView)
                            snap.addNewView(hittedFolder!!, movePos, 2, 2)
                            lastHittedView = hittedFolder // cuz folder replaced the app
                        }
                    }
                    is FolderView -> {
                        if (selected is AppView) {
                            action = INSERT_IN_FOLDER
                            hittedFolder = hittedView
                        }
                    }
                    else -> { action = -1 }
                }
            }
        }

        fun onUp(v: View, event: MotionEvent) {
            stageVP.getChildAt(0).onTouchEvent(event)
            val snap = currentSnapLayout
            if (inEditMode && selected != null) {
                when (action) {
                    MOVE -> {
                        val oldPos = (selected!!.layoutParams as SnapLayout.SnapLayoutParams).position
                        snap.moveView(selected!!, movePos)
                        // update data
                        if (selected is AppView) {
                            apps.remove(oldPos)
                            apps[movePos] = (selected as AppView).appInfo.id
                            DataKeeper.dumpUserStageApps(context)
                        }
                        // todo: data update
                    }
                    CREATE_FOLDER -> {
                        snap.removeView(selected!!)
                        hittedFolder!!.addApps(selected as AppView)
//                        save folder and apps (1 and selected removed)
                        // todo: data update
                    }
                    INSERT -> {

                    }
                    REMOVE -> {
                        snap.removeView(selected!!)
                        // todo: data update
                    }
                    INSERT_IN_FOLDER -> {
                        snap.removeView(selected!!)
                        hittedFolder!!.addApps(selected as AppView)
                        // todo: data update
                    }
                }
            }

            snap.removeView(ghostView)
            endAction()
        }

        override fun onLongPress(e: MotionEvent) = startEditMode()

        override fun onDown(e: MotionEvent?) = true

        fun select(v: View?, touchPoint: Point) {
            selected = v as? AppView ?: v as? FolderView
            selected?.let {
                //        val dy = ( it.top - (it.parent as ViewGroup).height / 2 ) * (1f - scaleInEditMode)
                it.animate().scaleX(scaleSelected).scaleY(scaleSelected)*/
/*.translationYBy(dy)*//*
.start()
                it.visibility = View.INVISIBLE
                (it as? AppView)?.animatorScale?.start()
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
            stageVP.animate().scaleX(scaleInEditMode).scaleY(scaleInEditMode).start()
        }

        private fun endEditMode() {
            stageVP.animate().scaleX(1f).scaleY(1f).start()
            inEditMode = false
        }

        private fun getPosSnapped(snap: SnapLayout, touchPoint: Point): Int {
            getLocationOnStage(snap, reusablePoint)
            localPoint.set(touchPoint.x - reusablePoint.x, touchPoint.y - reusablePoint.y)
            return snap.getPosSnapped(localPoint, 2)
        }
    }
*/

}