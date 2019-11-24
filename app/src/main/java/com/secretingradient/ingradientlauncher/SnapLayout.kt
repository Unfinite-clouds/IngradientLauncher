package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children
import java.io.Serializable

class SnapLayout : FrameLayout {

    // when we change snapCountX or snapCountY, we need to recompute children's LayoutParams
    var snapCountX = 0
    var snapCountY = 0
    var snapStepX = -1
    var snapStepY = -1

    val snapBounds = Rect()

    private val paddingReminder = Rect()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SnapLayout, 0, 0)
        snapCountX = a.getInteger(R.styleable.SnapLayout_snapCountX, 0)
        snapCountY = a.getInteger(R.styleable.SnapLayout_snapCountY, 0)
        snapBounds.set(0, 0, snapCountX, snapCountY)
        a.recycle()
    }
    constructor(context: Context, snapCountX: Int, snapCountY: Int) : super(context) {
        this.snapCountX = snapCountX
        this.snapCountY = snapCountY
        snapBounds.set(0, 0, snapCountX, snapCountY)
    }

    fun setSnapCounts(snapCountX: Int, snapCountY: Int) {
        this.snapCountX = snapCountX
        this.snapCountY = snapCountY
        snapBounds.set(0, 0, snapCountX, snapCountY)
    }

    override fun onViewAdded(child: View) {
        SnapLayoutParams.verifyLayoutParams(child.layoutParams)
        val lp = child.layoutParams as SnapLayoutParams
        lp.computeSnapBounds(this)
    }

    fun addNewView(child: View, layoutParams: SnapLayoutParams) {
        child.layoutParams = SnapLayoutParams(layoutParams)
        addView(child)
    }

    fun addNewView(child: View, layoutInfo: SnapLayoutInfo) {
        child.layoutParams = SnapLayoutParams(layoutInfo)
        addView(child)
    }

    fun addNewView(child: View, pos: Int, snapWidth: Int, snapHeight: Int) {
        child.layoutParams = SnapLayoutParams(pos, snapWidth, snapHeight)
        addView(child)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        println("...onMeasure")
        verify()

        val myWidth = MeasureSpec.getSize(widthMeasureSpec)
        val myHeight = MeasureSpec.getSize(heightMeasureSpec)


        if (myWidth != measuredWidth || myHeight != measuredHeight) {
            snapStepX = myWidth / snapCountX
            snapStepY = myHeight / snapCountY

            val reminderX = myWidth - snapStepX * snapCountX
            val reminderY = myHeight - snapStepY * snapCountY

            paddingReminder.set(0, 0, reminderX, reminderY)
        }

        measureChildren(0, 0)

        setMeasuredDimension(myWidth, myHeight)
    }

    override fun measureChild(child: View, widthSpec: Int, heightSpec: Int) {
        val lp = child.layoutParams as SnapLayoutParams

        child.measure(MeasureSpec.makeMeasureSpec(lp.snapWidth*snapStepX, MeasureSpec.EXACTLY),
                      MeasureSpec.makeMeasureSpec(lp.snapHeight*snapStepY, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        children.forEach { child: View ->
            val lp = child.layoutParams as SnapLayoutParams
            val l = lp.snapBounds.left * snapStepX
            val t = lp.snapBounds.top  * snapStepY
            child.layout(l, t, l + lp.snapWidth * snapStepX, t + lp.snapHeight * snapStepY)
        }
    }

    fun canPlaceView(v: View, ignore: View?): Boolean {
        SnapLayoutParams.verifyLayoutParams(v.layoutParams)
        return canPlaceHere(v.layoutParams as SnapLayoutParams, ignore)
    }

    fun canPlaceHere(p: Point, snapWidth: Int, snapHeight: Int): Boolean {
        val pos = snapToGrid(p, 2)
        return canPlaceHere(SnapLayoutParams(pos, snapWidth, snapHeight, this))
    }

    fun canPlaceHere(pos: Int, snapWidth: Int, snapHeight: Int): Boolean {
        return canPlaceHere(SnapLayoutParams(pos, snapWidth, snapHeight, this))
    }

    fun canMoveViewToPos(v: View, pos: Int, ignore: View? = null): Boolean {
        if (pos < 0) throw LauncherException("position= $pos must be positive")
        val lp = v.layoutParams as SnapLayoutParams
        val new_lp = SnapLayoutParams(pos, lp.snapWidth, lp.snapHeight, this)
        return canPlaceHere(new_lp, ignore)
    }

    private var last_lp_child: SnapLayoutParams? = null
    fun canPlaceHere(lp: SnapLayoutParams, excepted: View? = null): Boolean {
        if (!snapBounds.contains(lp.snapBounds))
            return false

        if (last_lp_child != null && Rect.intersects(lp.snapBounds, last_lp_child!!.snapBounds))
            return false

        children.forEach {
            if (it != excepted) {
                val lp_child = it.layoutParams as SnapLayoutParams
                if (Rect.intersects(lp.snapBounds, lp_child.snapBounds)) { // todo - bad using snapBounds
                    last_lp_child = lp_child
                    return false
                }
            }
        }
        return true
    }

    fun snapPoint(p: Point): Point {
        return Point(p.x / snapStepX * snapStepX, p.y / snapStepY * snapStepY)
    }

    fun getPointForPosition(position: Int): Point {
        val x = position % snapCountX * snapStepX
        val y = position / snapCountX * snapStepY
        return Point(x, y)
    }

    fun snapToGrid(p: Point, step: Int = 2): Int {
        // int division! Expression's order does matter
        var pos = p.x / snapStepX / step * step  +  p.y / snapStepY / step * step * snapCountX
        if (p.x >= width - paddingReminder.right)
            pos -= step
        if (p.y >= height - paddingReminder.bottom)
            pos -= step * snapCountX
        return pos
    }

    fun tryAddView(v: View, lp: SnapLayoutParams, p: Point): Boolean {
        val pos = snapToGrid(p)
        val saved_pos = lp.position  // save
        lp.position = pos
        lp.computeSnapBounds(this)
        if (canPlaceHere(lp)) {
            addView(v)
            return true
        }
        lp.position = saved_pos  // restore
        lp.computeSnapBounds(this) // bad code
        return false
    }

    fun moveView(v: View, pos: Int) {
        if (v.parent != this) throw LauncherException("view ${v.javaClass.simpleName} must be a child of SnapLayout to move")
        val lp = v.layoutParams as SnapLayoutParams
        if (lp.position != pos) {
            lp.position = pos
            lp.computeSnapBounds(this)
            requestLayout()
        }
    }

    private fun verify() {
        check(snapCountX > 0 && snapCountY > 0) {this}
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}={ " +
                "snapCountX=$snapCountX, " +
                "snapCountY=$snapCountY, " +
                "snapStepX=$snapStepX, " +
                "snapStepY=$snapStepY }"
    }


    class SnapLayoutParams(var position: Int, var snapWidth: Int, var snapHeight: Int) : LayoutParams(0, 0) {
        lateinit var snapBounds: Rect // need recompute if any property or snapCountX was changed

        constructor(info: SnapLayoutInfo) : this(info.position, info.snapWidth, info.snapHeight)
        constructor(layoutParams: SnapLayoutParams) : this(layoutParams.position, layoutParams.snapWidth, layoutParams.snapHeight)
        constructor(pos: Int, snapWidth: Int, snapHeight: Int, snapLayout: SnapLayout) : this(pos, snapWidth, snapHeight) {
            computeSnapBounds(snapLayout)
        }
        constructor(info: SnapLayoutInfo, snapLayout: SnapLayout) : this(info.position, info.snapWidth, info.snapHeight) {
            computeSnapBounds(snapLayout)
        }

        fun computeSnapBounds(snapLayout: SnapLayout) {
            snapBounds = Rect().apply {
                left = getPosX(snapLayout.snapCountX)
                top =  getPosY(snapLayout.snapCountX)
                right = left + snapWidth
                bottom = top + snapHeight
            }
            width = snapWidth * snapLayout.snapStepX
            height = snapHeight * snapLayout.snapStepY
        }

        private fun getPosX(snapCountX: Int): Int {
            return position % snapCountX
        }

        private fun getPosY(snapCountX: Int): Int {
            return position / snapCountX
        }

        override fun toString(): String {
            return "${this.javaClass.simpleName}={ position=$position, snapWidth=$snapWidth, snapHeight=$snapHeight }"
        }

        fun set(lp: SnapLayoutParams) {
            this.position = lp.position
            this.snapWidth = lp.snapWidth
            this.snapHeight = lp.snapHeight
        }

        fun set(pos: Int, snapWidth: Int, snapHeight: Int) {
            this.position = pos
            this.snapWidth = snapWidth
            this.snapHeight = snapHeight
        }

        fun verify() {
            check(position > -1 && snapWidth > 0 && snapHeight > 0) { this }
        }

        companion object {
            fun verifyLayoutParams(lp: ViewGroup.LayoutParams) {
                lp as? SnapLayoutParams ?: throw LauncherException("Invalid LayoutParams $lp")
                lp.verify()
            }
        }
    }


    class ElementHolder {
        var view: View?
            set(value) {
                field = value
                if (value != null && value.layoutParams !is SnapLayoutParams)
                    throw LauncherException("view.layoutParams must be SnapLayoutParams")
            }
        var snapPosition: Int
            get() = (view!!.layoutParams as SnapLayoutParams).position
            set(value) {(view!!.layoutParams as SnapLayoutParams).position = value}
        var snapWidth: Int
            get() = (view!!.layoutParams as SnapLayoutParams).snapWidth
            set(value) {(view!!.layoutParams as SnapLayoutParams).snapWidth = value}
        var snapHeight: Int
            get() = (view!!.layoutParams as SnapLayoutParams).snapHeight
            set(value) {(view!!.layoutParams as SnapLayoutParams).snapHeight = value}

        constructor(v: View? = null) {
            view = v
        }

        constructor(v: View, position: Int, snapWidth: Int, snapHeight: Int) {
            if (v.layoutParams !is SnapLayoutParams) {
                v.layoutParams = SnapLayoutParams(position, snapWidth, snapHeight)
            }
            view = v
        }

        fun setLayoutParams(lp: SnapLayoutParams) {
            (view!!.layoutParams as SnapLayoutParams).set(lp)
        }
    }

    data class SnapLayoutInfo(
        var position: Int = -1,
        var snapWidth: Int = -1,
        var snapHeight: Int = -1
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 33000L
        }

        constructor(info: SnapLayoutInfo) : this (info.position, info.snapWidth, info.snapHeight)

        fun copy(): SnapLayoutInfo {
            return SnapLayoutInfo(this)
        }

        fun verify() {
            check(position > -1 && snapWidth > 0 && snapHeight > 0) { this }
        }

        override fun toString(): String {
            return "${this.javaClass.simpleName}={ position=$position, snapWidth=$snapWidth, snapHeight=$snapHeight }"
        }
    }
}