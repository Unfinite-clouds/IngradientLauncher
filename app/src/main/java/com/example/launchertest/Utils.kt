package com.example.launchertest

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import androidx.preference.PreferenceManager
import kotlin.random.Random

fun toDp(px: Float): Float {
    return px / Resources.getSystem().displayMetrics.density
}

fun toDp (px: Int): Float {
    return px / Resources.getSystem().displayMetrics.density
}

fun toPx(dp: Float): Float {
    return dp * Resources.getSystem().displayMetrics.density
}

fun toPx (dp: Int): Float {
    return dp * Resources.getSystem().displayMetrics.density
}

class LauncherException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )

}

fun randomColor() : Int {
    return Color.rgb(Random.nextInt(255),Random.nextInt(255),Random.nextInt(255))
}

fun getPrefs(context: Context) : SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(context)
}