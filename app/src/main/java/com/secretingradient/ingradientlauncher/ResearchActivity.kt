@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.stage.UserStage
import kotlinx.android.synthetic.main.research_layout.*


class ResearchActivity : AppCompatActivity() {

    var value = 0
    val apps = mutableListOf<UserStage.SnapElementInfo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.research_layout)

        fillApps()

        apps.forEach {
            snap_layout.addView(AppView(this, it.appInfo).apply {
                this.setOnTouchListener(this@ResearchActivity.research_root)
            }, it.snapLayoutInfo)
        }

        snap_layout.setOnTouchListener(research_root)
        research_root.setOnTouchListener(research_root)

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
            apps.add(i, UserStage.SnapElementInfo(allApps[i], SnapLayout.SnapLayoutInfo(i*2 + (i*2/8)*8, 2, 2)))
        }
    }


}


class MyRoot : LinearLayout, View.OnTouchListener {
    var selected: View? = null
    var isEditMode = false
    val gListener = GestureListener()
    val gDetector = GestureDetector(context, gListener)
    var startPoint = Point()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isEditMode && selected != null)
            return true

        if (ev.action == MotionEvent.ACTION_DOWN)
            return false

        return false
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        gDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                println("ACTION_DOWN selected = $selected, v = $v")
                isEditMode = true
                selected = v
                startPoint = Point(event.x.toInt() - v.left, event.y.toInt() - v.top)
            }

            MotionEvent.ACTION_MOVE -> {
                println("ACTION_MOVE ev.x = ${event.x} selected = $selected, v = $v")
                if (v == this && selected != null) {
                    selected!!.translationX = event.x - selected!!.left
                    selected!!.translationY = event.y - selected!!.top
                }
            }

            MotionEvent.ACTION_UP -> {
                if (selected !is AppView) {
                    println("end EditMode with selected = $selected, v = $v")
                    isEditMode = false
                }
                selected = null
            }
        }

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

}


class MyLinearLayout : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

}
