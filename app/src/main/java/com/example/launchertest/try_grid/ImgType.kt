package com.example.launchertest.try_grid

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class ImgType : ImageView{
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


/*    override fun onAttachedToWindow() {
        println("ImgType onAttachedToWindow")
        super.onAttachedToWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        println("ImgType onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        println("ImgType layout")
        super.layout(l, t, r, b)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        println("ImgType onLayout $width, $measuredWidth, $minimumWidth, $maxWidth")
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        println("ImgType dispatchTouchEvent")
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        println("ImgType onTouchEvent")
        return super.onTouchEvent(event)
    }*/
}