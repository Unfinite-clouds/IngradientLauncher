package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import kotlin.math.ceil
import kotlin.math.floor

class SnapLayout : FrameLayout {

    // when we change snapCountX or snapCountY, we need to recompute children's LayoutParams
    private var snapCountX = 0
    private var snapCountY = 0
    private var snapStepX = -1
    private var snapStepY = -1

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
        SnapLayoutParams.verifyLayoutParams(child)
        val lp = child.layoutParams as SnapLayoutParams
        lp.computeBounds(snapCountX, snapCountY)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        verify()

        val myWidth = MeasureSpec.getSize(widthMeasureSpec)
        val myHeight = MeasureSpec.getSize(heightMeasureSpec)

        if (myWidth == measuredWidth && myHeight == measuredHeight) {
            setMeasuredDimension(myWidth, myHeight)
            return
        }

        snapStepX = myWidth / snapCountX
        snapStepY = myHeight / snapCountY

        val reminderX = myWidth - snapStepX*snapCountX
        val reminderY = myHeight - snapStepY*snapCountY

        paddingInternal.set(
            floor(reminderX.toFloat()/2f).toInt(),
            floor(reminderY.toFloat()/2f).toInt(),
            ceil(reminderX.toFloat()/2f).toInt(),
            ceil(reminderY.toFloat()/2f).toInt())

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
            val l = lp.bounds.left * snapStepX
            val t = lp.bounds.top  * snapStepY
            child.layout(l, t, l + child.measuredWidth, t + child.measuredHeight)
        }
    }

    fun canLayoutView(v: View): Boolean {
        SnapLayoutParams.verifyLayoutParams(v)
        val lp_v = v.layoutParams as SnapLayoutParams
        children.forEach {
            val lp = it.layoutParams as SnapLayoutParams
            if (Rect.intersects(lp_v.bounds, lp.bounds))
                return false
        }
        return true
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


    class SnapLayoutParams : LayoutParams {
        var position = -1
        var snapWidth = -1
        var snapHeight = -1
        val bounds = Rect()

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs)
        constructor(pos: Int, snapWidth: Int, snapHeight: Int) : super(0, 0) {
            this.position = pos
            this.snapWidth = snapWidth
            this.snapHeight = snapHeight
        }

        fun computeBounds(snapCountX: Int, snapCountY: Int) {
            bounds.apply {
                left = getPosX(snapCountX)
                top =  getPosY(snapCountY)
                right = left + snapWidth
                bottom = top + snapHeight
            }
        }

        private fun getPosX(snapCountX: Int): Int {
            return position % snapCountX
        }

        private fun getPosY(snapCountY: Int): Int {
            return position / snapCountY
        }

        fun verify() {
            check(position > -1 && snapWidth > 0 && snapHeight > 0) {this}
        }

        override fun toString(): String {
            return "${this.javaClass.simpleName}={ position=$position, snapWidth=$snapWidth, snapHeight=$snapHeight, width=$width, height=$height }"
        }

        companion object {
            fun verifyLayoutParams(v: View) {
                val lp = v.layoutParams
                lp as? SnapLayoutParams ?: throw LauncherException("Invalid LayoutParams $lp of view $v.")
                lp.verify()
            }
        }
    }
}