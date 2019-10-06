package com.example.launchertest

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat

class TrashView : ImageView{
    init {
        deactivate()
    }
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun activate() {
        setBackgroundResource(R.color.TransparentWhite)
        setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, null))
    }

    fun deactivate() {
        setBackgroundResource(R.color.Transparent)
        setImageDrawable(null)
    }
}