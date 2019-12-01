package com.secretingradient.ingradientlauncher

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.data.DataKeeper
import com.secretingradient.ingradientlauncher.drag.DragController
import com.secretingradient.ingradientlauncher.stage.BaseStage2

class Launcher : FrameLayout {
    lateinit var launcherActivity: LauncherActivity
        private set
    lateinit var launcherVP: ViewPager2
        private set
    lateinit var launcherRV: RecyclerView
        private set
    lateinit var wallpaper: WallpaperFlow
        private set
    val gestureHelper: GestureHelper
        get() = launcherActivity.gestureHelper
    val dataKeeper: DataKeeper
        get() = launcherActivity.dataKeeper
    val dragController: DragController
        get() = launcherActivity.dragController
    val currentStage: BaseStage2
        get() = stages[launcherVP.currentItem]

    val stages = mutableListOf<BaseStage2>()
    var dispatchToCurrentStage = false
    var isAnimating = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun initViewPager(viewPager: ViewPager2) {
        launcherActivity = context as LauncherActivity
        launcherVP = viewPager
        launcherRV = launcherVP[0] as RecyclerView
        launcherVP.adapter = LauncherAdapter(context, stages)
        launcherVP.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                isAnimating = state != 0
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        wallpaper = WallpaperFlow(context, windowToken)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//        if (ev.action == MotionEvent.ACTION_DOWN) {
//            dispatchToCurrentStage = false
//        }
        gestureHelper.onTouchEvent(ev)

        var handled: Boolean

        handled = dragController.onTouchEvent(ev)

        // TODO: DEPRECATED
/*        if (!handled) {
            // dispatch to proper itemView - it will current viewHolder.itemView
            if (dispatchToCurrentStage) {
                // attention! when we do that way - we ignore launcherVP scroll listener (for scroll up/down)
                handled = getCurrentItemView()?.stage?.dispatchTouchEvent(ev) ?: false.also {
                    println("Warning: viewHolder for stage ${launcherViewPager.currentItem} was not found, but dispatchToCurrentStage == true")
                }
            }
        }*/

        if (!handled)
            handled = super.dispatchTouchEvent(ev)

        return handled
    }

    fun getCurrentItemView(): StageHolder? {
        return launcherRV.findViewHolderForLayoutPosition(launcherVP.currentItem) as? StageHolder
    }

    private fun scrollToStage(i: Int) {
        launcherVP.currentItem = i
    }

    inner class LauncherAdapter(val context: Context, val stages: MutableList<BaseStage2>) : RecyclerView.Adapter<StageHolder>() {
        override fun getItemCount(): Int = 1  // todo: 4
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =  StageHolder(FrameLayout(context))
        override fun onBindViewHolder(holder: StageHolder, position: Int) {
            // todo: shit method
            val root = holder.stage
            holder.stage.removeAllViews()
            if (stages.getOrNull(position) == null) {
                println("inflating new stage $position")
                val stageId = when (position) {
                    0 -> R.layout.stage_1_user
//            1 -> R.layout.stage_1_user
//            2 -> R.layout.stage_2_all_apps
//            3 -> R.layout.stage_3_all_widgets
                    else -> throw LauncherException("position must be in range 0..${itemCount - 1}")
                }
                val newStage = inflateStage(stageId, root)
                stages.add(newStage)
            }
            root.addView(stages[position]) // seems buggy
        }
        private fun inflateStage(resourceId: Int, root: ViewGroup) : BaseStage2 {
            return LayoutInflater.from(context).inflate(resourceId, root, false) as BaseStage2
        }
    }

    class StageHolder(val stage: FrameLayout) : RecyclerView.ViewHolder(stage) {
        init {stage.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)}
    }
}