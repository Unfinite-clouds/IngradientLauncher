package com.secretingradient.ingradientlauncher

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.preference.PreferenceManager
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.FolderView
import com.secretingradient.ingradientlauncher.element.WidgetView
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.*
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

class LauncherException : Throwable {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}

fun randomColor() : Int {
    return Color.rgb(Random.nextInt(255),Random.nextInt(255),Random.nextInt(255))
}

fun getPrefs(context: Context) : SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(context)
}

fun encodeBitmap(bitmap: Bitmap, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): ByteArray {
    val byteArrayOS = ByteArrayOutputStream()
    bitmap.compress(compressFormat, quality, byteArrayOS)
    return byteArrayOS.toByteArray()
}

fun decodeBitmap(byteArray: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

fun printFile(context: Context, file: String){
    val inputStream = InputStreamReader(context.openFileInput(file))
    println(inputStream.readText())
    inputStream.close()
}

fun MutableList<*>.swap(i: Int, j: Int) {
    Collections.swap(this, i, j)
}

fun vibrate(context: Context, time: Long = 70L) {
    if (Build.VERSION.SDK_INT >= 26) {
        (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(time)
    }
}

fun printlnClass(message: String, vararg any: Any?) {
    print(message)
    any.forEachIndexed { index, obj ->
        if (index != 0)
            print(", ")
        if (obj is Number || obj is String)
            print(obj)
        else
            println(obj?.javaClass?.simpleName)
    }
    println()
}

fun Any?.className(): String? {
    return this?.javaClass?.simpleName
}

fun isElement(v: View?): Boolean {
    return v as? AppView ?: v as? FolderView ?: v as? WidgetView != null
}

fun View.setSnapLayoutParams(position: Int, snapW: Int = 2, snapH: Int = 2) {
    layoutParams = SnapLayout.SnapLayoutParams(position, snapW, snapH)
}

fun <K, V> MutableMap<K, V>.move(from: K, to: K) {
    if (from == to)
        return
    if (this[from] == null) throw LauncherException("item at index $from is absent")
    if (this[to] != null) throw LauncherException("item at index $to is busy. attempt to rewrite")

    this[to] = this[from]!!
    this.remove(from)
}

fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to)
        return
    if (this[from] == null) throw LauncherException("item at index $from is absent")
    if (this[to] != null) throw LauncherException("item at index $to is busy. attempt to rewrite")

    this[to] = this[from]!!
    this.removeAt(from)
}

fun <T> MutableList<T>.moveStack(from: Int, to: Int) {
    if (from == to)
        return
    val tmp = this[from]
    val direction = if (to > from) 1 else -1
    val range = if (to > from) from until to else from downTo to - direction
    for (i in range) {
        this[i] = this[i + direction]
    }
    this[to] = tmp
}


fun <K, V> MutableMap<K, V>.swap(from: K, to: K) {
    if (from == to)
        return
    if (this[to] == null) throw LauncherException("item at index $to is absent")
    if (this[from] == null) throw LauncherException("item at index $from is absent")

    val tmp = this[to]
    this[to] = this[from]!!
    this[from] = tmp!!
}
