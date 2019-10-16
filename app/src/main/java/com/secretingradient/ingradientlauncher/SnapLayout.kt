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
import kotlin.math.ceil
import kotlin.math.floor

class SnapLayout : FrameLayout {

    // when we change snapCountX or snapCountY, we need to recompute children's LayoutParams
    var snapCountX = 0
    var snapCountY = 0
    var snapStepX = -1
    var snapStepY = -1

    private var paddingInternal = Rect()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SnapLayout, 0, 0)
        snapCountX = a.getInteger(R.styleable.SnapLayout_snapCountX, 0)
        snapCountY = a.getInteger(R.styleable.SnapLayout_snapCountY, 0)
        a.recycle()
    }
    constructor(context: Context, snapCountX: Int, snapCountY: Int) : super(context) {
        this.snapCountX = snapCountX
        this.snapCountY = snapCountY
    }

    override fun onViewAdded(child: View) {
        SnapLayoutParams.verifyLayoutParams(child.layoutParams)
        val lp = child.layoutParams as SnapLayoutParams
        lp.computeSnapBounds(snapCountX)
    }

    fun addView(child: View, layoutInfo: SnapLayoutInfo) {
        child.layoutParams = SnapLayoutParams(layoutInfo)
        addView(child)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        println("...onMeasure")
        verify()

        val myWidth = MeasureSpec.getSize(widthMeasureSpec)
        val myHeight = MeasureSpec.getSize(heightMeasureSpec)


        if (myWidth != measuredWidth || myHeight != measuredHeight) {
            snapStepX = myWidth / snapCountX
            snapStepY = myHeight / snapCountY

            val reminderX = myWidth - snapStepX * snapCountX
            val reminderY = myHeight - snapStepY * snapCountY

            paddingInternal.set(
                floor(reminderX.toFloat() / 2f).toInt(),
                floor(reminderY.toFloat() / 2f).toInt(),
                ceil(reminderX.toFloat() / 2f).toInt(),
                ceil(reminderY.toFloat() / 2f).toInt()
            )
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
        println("...onLayout")
        children.forEach { child: View ->
            val lp = child.layoutParams as SnapLayoutParams
            val l = lp.snapBounds.left * snapStepX
            val t = lp.snapBounds.top  * snapStepY
            child.layout(l, t, l + lp.snapWidth * snapStepX, t + lp.snapHeight * snapStepY)
        }
    }

    fun canPlaceView(v: View): Boolean {
        SnapLayoutParams.verifyLayoutParams(v.layoutParams)
        return canPlaceHere(v.layoutParams as SnapLayoutParams)
    }

    fun canPlaceHere(p: Point, snapWidth: Int, snapHeight: Int): Boolean {
        val pos = getPosSnapped(p, 2)
        return canPlaceHere(SnapLayoutParams(pos, snapWidth, snapHeight, snapCountX))
    }

    fun canPlaceHere(pos: Int, snapWidth: Int, snapHeight: Int): Boolean {
        return canPlaceHere(SnapLayoutParams(pos, snapWidth, snapHeight, snapCountX))
    }

    fun canPlaceHere(layoutInfo: SnapLayoutInfo): Boolean {
        return canPlaceHere(SnapLayoutParams(layoutInfo, snapCountX))
    }

    private var last_lp_child: SnapLayoutParams? = null
    fun canPlaceHere(lp: SnapLayoutParams): Boolean {
        if (last_lp_child != null && Rect.intersects(lp.snapBounds, last_lp_child!!.snapBounds))
            return false

        children.forEach {
            val lp_child = it.layoutParams as SnapLayoutParams
            if (Rect.intersects(lp.snapBounds, lp_child.snapBounds)) {
                last_lp_child = lp_child
                return false
            }
        }
        return true
    }

    fun getPointSnapped(p: Point): Point {
        return Point(p.x / snapStepX * snapStepX, p.y / snapStepY * snapStepY)
    }

    fun getPosSnapped(p: Point, step: Int = 2): Int {
        // int division! Order does matter
        return p.x / snapStepX / step * step  +  p.y / snapStepY / step * step * snapCountX
    }

    fun tryAddView(v: View, lp: SnapLayoutParams, p: Point): Boolean {
        val pos = getPosSnapped(p)
        val saved_pos = lp.position  // save
        lp.position = pos
        lp.computeSnapBounds(snapCountX)
        if (canPlaceHere(lp)) {
            addView(v)
            return true
        }
        lp.position = saved_pos  // restore
        lp.computeSnapBounds(snapCountX) // bad code
        return false
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
        lateinit var snapBounds: Rect // need recompute if any property or snapCountX was change

        constructor(info: SnapLayoutInfo) : this(info.position, info.snapWidth, info.snapHeight)
        constructor(pos: Int, snapWidth: Int, snapHeight: Int, snapCountX: Int) : this(pos, snapWidth, snapHeight) {
            computeSnapBounds(snapCountX)
        }
        constructor(info: SnapLayoutInfo, snapCountX: Int) : this(info.position, info.snapWidth, info.snapHeight) {
            computeSnapBounds(snapCountX)
        }

        fun computeSnapBounds(snapCountX: Int) {
            snapBounds = Rect().apply {
                left = getPosX(snapCountX)
                top =  getPosY(snapCountX)
                right = left + snapWidth
                bottom = top + snapHeight
            }
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


    data class SnapLayoutInfo(
        var position: Int = -1,
        var snapWidth: Int = -1,
        var snapHeight: Int = -1
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 4401L
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