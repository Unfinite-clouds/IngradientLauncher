package com.secretingradient.ingradientlauncher

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class GhostView : View {
    var view: View? = null
    constructor(context: Context, view: View?) : super(context) {this.view = view}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onDraw(canvas: Canvas) {
        view?.draw(canvas)
    }
}