@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.ingradientlauncher

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.secretingradient.ingradientlauncher.element.AppInfo
import com.secretingradient.ingradientlauncher.element.AppView
import kotlinx.android.synthetic.main.research_layout.*


class ResearchActivity : AppCompatActivity() {

    var value = 0
    lateinit var apps: MutableList<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.research_layout)

        fillApps()

        snap_layout.addView(AppView(this, apps[0]).apply {
            layoutParams = SnapLayout.SnapLayoutParams(1, 2,2)
        })

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
        apps = AppManager.allApps.values.toMutableList()
        for (i in 0..15) {
            apps.add(i, apps[i])
        }
    }
}


class MyRoot : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

}


class MyLinearLayout : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

}
