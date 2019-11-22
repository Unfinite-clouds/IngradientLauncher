package com.secretingradient.ingradientlauncher

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.data.DataKeeper
import com.secretingradient.ingradientlauncher.stage.BaseStage
import com.secretingradient.ingradientlauncher.stage.StageAdapter

class LauncherRootLayout : FrameLayout {
    lateinit var launcherViewPager: ViewPager2
        private set
    lateinit var launcherRecyclerView: RecyclerView
        private set
    lateinit var dataKeeper: DataKeeper
        private set
    lateinit var wallpaper: WallpaperFlow
        private set

    val stages = mutableListOf<BaseStage>()
    var dispatchToCurrentStage = false
    var isAnimating = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun initViewPager(viewPager: ViewPager2, dataKeeper: DataKeeper) {
        launcherViewPager = viewPager
        launcherViewPager.adapter = StageAdapter(this)
        launcherRecyclerView = launcherViewPager[0] as RecyclerView
        this.dataKeeper = dataKeeper
        launcherViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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
        if (ev.action == MotionEvent.ACTION_DOWN)
            dispatchToCurrentStage = false

        // dispatch to proper itemView - it will current viewHolder.itemView
        if (dispatchToCurrentStage) {
            // attention! when we do that way - we ignore launcherVP scroll listener (for scroll up/down)
            return getCurrentItemView()?.stageRootLayout?.dispatchTouchEvent(ev) ?: super.dispatchTouchEvent(ev).also {
                    println("Warning: viewHolder for stage ${launcherViewPager.currentItem} was not found, but dispatchToCurrentStage == true")
                }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun getCurrentItemView(): StageAdapter.StageHolder? {
        return launcherRecyclerView.findViewHolderForLayoutPosition(launcherViewPager.currentItem) as? StageAdapter.StageHolder
    }

    private fun scrollToStage(number: Int) {
        launcherViewPager.currentItem = number
    }

    fun transferEvent(stage: Int, obj: Any? = null) {
        scrollToStage(stage)
        stages[stage].receiveTransferEvent(obj)
    }
}