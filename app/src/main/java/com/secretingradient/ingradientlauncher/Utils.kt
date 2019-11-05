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
import androidx.preference.PreferenceManager
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