package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View

class GhostView : View {
    var view: View? = null
    init {
        setBackgroundColor(Color.LTGRAY)
    }
    constructor(context: Context, view: View?) : super(context) {
        this.view = view
    }
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onDraw(canvas: Canvas) {
        view?.draw(canvas)
    }
}