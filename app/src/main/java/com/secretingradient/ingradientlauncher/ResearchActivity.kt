@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.ingradientlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.withTranslation
import androidx.core.view.children
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.element.ElementInfo
import kotlinx.android.synthetic.main._research_layout.*


class ResearchActivity : AppCompatActivity() {

    var value = 0
    val apps = mutableListOf<ElementInfo>()
    val stages = mutableListOf<Stage>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout._research_layout)

//        fillApps()

        research_root.vp = research_vp
        research_root.stages = stages
        (research_vp[0] as ViewGroup).clipChildren = false
        research_vp.offscreenPageLimit = 2

        research_vp.adapter = object : RecyclerView.Adapter<StageVH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StageVH {
                return StageVH(
                    (LayoutInflater.from(parent.context).inflate(R.layout._research_item, parent, false) as Stage)
                    .apply { stages.add(this); stageNumber = stages.size-1; mainRoot = this@ResearchActivity.research_root }
                )
            }

            override fun onBindViewHolder(holder: StageVH, position: Int) {
                holder.tv.text = position.toString()
            }

            override fun getItemCount() = 3
        }

    }

    class StageVH(val stage: Stage) : RecyclerView.ViewHolder(stage) {
        val tv = stage.findViewById<TextView>(R.id.research_text)
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    fun fillApps() {
        DataKeeper.init(this)
//        val allApps = DataKeeper.allApps.values.toList()
        for (i in 0..6) {
//            apps.add(i, ElementInfo(allApps[i], SnapLayout.SnapLayoutInfo(i*2 + (i*2/8)*8, 2, 2)))
        }
    }


}


class MyRoot : LinearLayout {

    lateinit var vp: ViewPager2
    var misdirection: View? = null
    lateinit var stages: List<Stage>
    var dispatchToCurrent = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // dispatch to proper itemView - it will current viewHolder.itemView
        // getCurrentItemView()
        if (dispatchToCurrent) {
            return getCurrentItemView().stage.dispatchTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun getCurrentItemView(): ResearchActivity.StageVH {
        return (vp[0] as RecyclerView).findViewHolderForLayoutPosition(vp.currentItem) as ResearchActivity.StageVH
    }

/*    var selected: TextView? = null
    val point = Point()

    @SuppressLint("NewApi")
    val mStageTouchListener = OnTouchListener { v, event ->
        // handling event in context of current ViewGroup item in ViewPager2
        v as ViewGroup
        println("${stages.indexOf(v)} ${MotionEvent.actionToString(event.action)}")
        point.set(event.x.toInt(), event.y.toInt())
        val viewUnderPointer = getHitViewAt(v, point)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selected = viewUnderPointer as? TextView
                if (selected != null) {
                    v.requestDisallowInterceptTouchEvent(true)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (selected != null) {

                    drag(selected!!, point)
                    if (viewUnderPointer is ImageView) {
                        vp.currentItem = 1
                    }
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                // touch intercepted by VP
                selected = null
            }

            MotionEvent.ACTION_UP -> {
                selected = null
            }
        }

        true
    }

    private var lastHited: View? = null
    private val hitRect = Rect()
    private fun getHitViewAt(v: ViewGroup, p: Point): View? {
        lastHited?.getHitRect(hitRect)
        if (lastHited != null && hitRect.contains(p.x, p.y)) {
            return lastHited!!
        }

        v.children.forEach {
            it.getHitRect(hitRect)
            if (hitRect.contains(p.x, p.y)) {
                lastHited = it
                return it
            }
        }

        return null
    }
*/

}


class Stage : FrameLayout {
    lateinit var mainRoot: MyRoot
    var stageNumber: Int = -1
//    val overlayPoint = Point()

    constructor(context: Context, n: Int) : super(context) { this.stageNumber = n}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private val point = Point()
    private var selected: TextView? = null
    var action = 0
    val INSERT = 1

    @SuppressLint("NewApi")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // handling event in context of current ViewGroup item in ViewPager2
        println("$stageNumber ${MotionEvent.actionToString(event.action)}")
        point.set(event.x.toInt(), event.y.toInt())
        val viewUnderPointer = getHitViewAt(point)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                select(viewUnderPointer)
            }

            MotionEvent.ACTION_MOVE -> {
                if (selected != null) {
                    invalidate()
                    if (viewUnderPointer is ImageView && action != INSERT) {
                        mainRoot.vp.currentItem = 1
                        val receiverStage = mainRoot.stages[1]
                        receiverStage.select(selected)
                        receiverStage.action = INSERT
                        removeView(selected!!)
                        unselect()
                        action = 0
                    }
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                // touch intercepted by VP
                endAction()
            }

            MotionEvent.ACTION_UP -> {
                if (action == INSERT) {
                    selected!!.let {
                        it.translationX = point.x.toFloat() - it.left
                        it.translationY = point.y.toFloat() - it.top
                        addView(it)
                    }
                }
                endAction()
            }
        }
        return true
    }

    fun select(v: View?) {
        selected = v as? TextView
        if (selected != null) {
            requestDisallowInterceptTouchEvent(true)
            mainRoot.dispatchToCurrent = true
            selected!!.visibility = View.INVISIBLE
        }
    }

    fun unselect() {
        selected?.visibility = View.VISIBLE
        selected = null
        invalidate()
    }

    fun endAction() {
        unselect()
        mainRoot.dispatchToCurrent = false
        action = 0
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (selected != null) {
            canvas?.withTranslation(point.x.toFloat(), point.y.toFloat()) {
                selected?.draw(canvas)
            }
        }
    }


    private var lastHited: View? = null
    private val hitRect = Rect()
    private fun getHitViewAt(p: Point): View? {
        lastHited?.getHitRect(hitRect)
        if (lastHited != null && hitRect.contains(p.x, p.y)) {
            return lastHited!!
        }

        children.forEach {
            it.getHitRect(hitRect)
            if (hitRect.contains(p.x, p.y)) {
                lastHited = it
                return it
            }
        }

        return null
    }
}
