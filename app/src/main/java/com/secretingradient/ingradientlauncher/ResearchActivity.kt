@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.data.DataKeeper


class ResearchActivity : AppCompatActivity() {
    lateinit var dk: DataKeeper
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout._research_layout)
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}



class MyRoot : FrameLayout {
    val paint: Paint = Paint().apply { color = Color.GREEN; strokeWidth = 3f; style = Paint.Style.STROKE }
    val path: Path = Path()
    val m: Matrix = Matrix()
    val w = 200f
    val h = 400f
    val rSrc: RectF = RectF(0f, 0f, w, h)
    val r = RectF()
    val p = FloatArray(2)
    val pivot1 = FloatArray(2).apply{ this[0] = w/2; this[1] = h/2 }
    val pivot2 = FloatArray(2).apply{ this[0] = w/2; this[1] = h/2 }
    val m2 = Matrix()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
/*        m.postTranslate(100f, 100f)

        paint.color = Color.GREEN
        m.mapRect(r, rSrc)
        m.mapPoints(p, pivot1)
        canvas.drawRect(r, paint)
        canvas.drawCircle(p[0], p[1], 2f, paint)

        paint.color = Color.BLACK
        m.preScale(0.7f, 0.7f, pivot1[0], pivot1[1])
        m.postTranslate(0f, 0f)
        m.mapRect(r, rSrc)
        m.mapPoints(p, pivot2)
        canvas.drawRect(r, paint)
        canvas.drawCircle(p[0], p[1], 2f, paint)


        r.set(0f, 100f, w, h)
        // parents transform
        m2.set(m)
        // self transform
        m2.preScale(0.4f, 0.4f, pivot2[0], pivot2[1])
        m2.postTranslate(0f, 100f)
        m2.mapRect(r)
        toast(context, "${r.contains(200f, 400f)}")  // 172, 372 - 228 - 456

        paint.color = Color.RED
        m.preScale(0.4f, 0.4f, pivot1[0], pivot1[1])
        m.postTranslate(0f, 100f)
        m.mapRect(r, rSrc)
        canvas.drawRect(r, paint)*/

    }
}

class TransformMatrix {
    val scale = PointF()
    val translation = PointF()

    fun translate(dx: Float, dy: Float) {
        translation.x += dx
        translation.y += dy
    }

    fun scale(sx: Float, sy: Float) {
        scale.x *= sx
        scale.y *= sy
    }
}

class TestStage(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    lateinit var frame: FrameLayout

    override fun onFinishInflate() {
        super.onFinishInflate()
        frame = getChildAt(0) as FrameLayout
        scaleX = 0.7f
        scaleY = 0.7f
        frame.scaleX = 0.4f
        frame.scaleY = 0.4f
    }

    val reusablePoint = Point()
    val reusablePointF = FloatArray(2)
    val reusableRect = Rect()
    val reusableRectF = RectF()
    val reusableMatrix = Matrix()
    val tmpMatrix = Matrix()
    val transformMatrix = TransformMatrix()

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {


/*        getGlobalVisibleRect(reusableRect)
        println("getGlobalVisibleRect - $reusableRect")
        getHitRect(reusableRect)
        println("getHitRect - $reusableRect")
        getLocalVisibleRect(reusableRect)
        println("getLocalVisibleRect - $reusableRect")
        println("layout - $left, $top, $right, $bottom")
        getDrawingRect(reusableRect)
        println("getDrawingRect - $reusableRect")
        println("WxH - $width x $height")
        getLocationOnScreen(reusablePoint.asArray())
        println("getLocationOnScreen - $reusablePoint")*/

        var nextParent: ViewGroup? =  parent as? ViewGroup

        reusableMatrix.reset()
        nextParent?.getLocationOnScreen(reusablePoint.asArray())  // scaled point
        reusableMatrix.postTranslate(reusablePoint.x.toFloat(), reusablePoint.y.toFloat())
        // but all parent's scales are not applied to matrix.
        // Not working if any parent was scaled

        var hittedView: View?
        var view: View? = null

        while (nextParent is ViewGroup) {
            hittedView = null
            for (i in nextParent.childCount - 1 downTo 0) {
                val child = nextParent.getChildAt(i)
                if (child.isVisible && hitView(child, ev.rawX, ev.rawY)) {
                    hittedView = child
                    view = child
                    break
                }
            }
            nextParent = hittedView as? ViewGroup ?: break
            // shift to parent coords
            reusableMatrix.preTranslate(nextParent.x, nextParent.y)
            reusableMatrix.preScale(nextParent.scaleX, nextParent.scaleY, nextParent.pivotX, nextParent.pivotY)
//            reusableMatrix.postTranslate(nextParent.x + (1-nextParent.scaleX)*nextParent.pivotX, nextParent.y + (1-nextParent.scaleY)*nextParent.pivotY)
        }

        println(view)
        return true
    }

    fun hitView(v: View, x: Float, y: Float): Boolean {
//        v.getHitRect(reusableRect)
        reusableRectF.set(0f, 0f, v.width.toFloat(), v.height.toFloat())

        // parent transform
        tmpMatrix.set(reusableMatrix)
        // self transform
        tmpMatrix.preTranslate(v.x, v.y)
        tmpMatrix.preScale(v.scaleX, v.scaleY, v.pivotX, v.pivotY)
        tmpMatrix.mapRect(reusableRectF)

        return reusableRectF.contains(x, y)
    }

}







class ItemDragger : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
    }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}