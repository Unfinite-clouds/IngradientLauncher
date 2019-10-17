@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.PointF
import android.os.Bundle
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.SnapElementInfo
import kotlinx.android.synthetic.main.research_layout.*


class ResearchActivity : AppCompatActivity() {

    var value = 0
    val apps = mutableListOf<SnapElementInfo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.research_layout)

        fillApps()

//        apps.forEach {
//            snap_layout.addNewView(AppView(this, it.appInfo).apply {
//                this.setOnTouchListener(this@ResearchActivity.research_root)
//            }, it.snapLayoutInfo)
//        }

//        snap_layout.setOnTouchListener(research_root)
        research_root.setOnTouchListener(research_root)
//        edit_text.setOnTouchListener(research_root)

        edit_text.setOnEditorActionListener { v, actionId, event ->
            v as EditText
            val text = v.text.toString()
            value = if (text != "") text.toInt() else 0
            hideKeyboard()
            return@setOnEditorActionListener true
        }
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    fun fillApps() {
        AppManager.loadAllApps(this)
        val allApps = AppManager.allApps.values.toList()
        for (i in 0..6) {
            apps.add(i, SnapElementInfo(allApps[i], SnapLayout.SnapLayoutInfo(i*2 + (i*2/8)*8, 2, 2)))
        }
    }


}


class MyRoot : LinearLayout, View.OnTouchListener {

    var selected: View? = null
    var isEditMode = false
    val gListener = GestureListener()
    val gDetector = GestureDetector(context, gListener)
    val ghostView = ImageView(context).apply { setBackgroundColor(Color.BLUE) }
    val pointer = PointF()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

/*    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (selected != null)
            return true

        return false
    }*/

    override fun onTouch(v: View, event: MotionEvent): Boolean {
/*        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                println("ACTION_DOWN selected = $selected, v = $v")
                selected = v as? AppView
            }

            MotionEvent.ACTION_MOVE -> {
                println("ACTION_MOVE ev.x = ${event.x} selected = $selected, v = $v")

                if (selected == null) {
                    return false
                }

//                selected!!.translationX = event.x - selected!!.left
//                selected!!.translationY = event.y - selected!!.top
                selected!!.visibility = View.INVISIBLE
                pointer.set(event.x, event.y)

                val hitRect = Rect()

                snap_layout.getHitRect(hitRect)
                if (hitRect.contains(event.x.toInt(), event.y.toInt())) {
                    println("in snap_layout")
                    val local = getLocalPoint(snap_layout, event)
                    snap_layout.removeView(ghostView)
                    if (snap_layout.canPlaceHere(local, 2, 2)) {
                        println("can be placed")
                        snap_layout.addNewView(ghostView, SnapLayout.SnapLayoutInfo(snap_layout.getPosSnapped(local, 2), 2, 2))
                    }
                }

                research_img.getHitRect(hitRect)
                if (hitRect.contains(event.x.toInt(), event.y.toInt())) {
                    println("in research_img")
                    research_img.setBackgroundColor(Color.BLUE)
                    invalidate()
                } else {
                    research_img.setBackgroundColor(Color.TRANSPARENT)
                }

            }

            MotionEvent.ACTION_UP -> {
                println("ACTION_MOVE = $selected, v = $v")
                selected?.let {
//                    it.translationX = 0f
//                    it.translationY = 0f
                    it.visibility = View.VISIBLE
                }
                selected = null
            }
        }*/

        return true
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent?) {
            if (selected is AppView) {
                println("start EditMode with selected = $selected")
                selected?.setBackgroundColor(Color.YELLOW)
                isEditMode = true
            }
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            return super.onDoubleTap(e)
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {

            return true
        }
    }

    fun getLocalPoint(child: View, event: MotionEvent): Point {
        return Point(event.x.toInt() - child.left, event.y.toInt() - child.top)
    }


/*    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.withTranslation(pointer.x, pointer.y) {
            selected?.draw(canvas)
        }
    }*/
}


class MyLinearLayout : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

}
