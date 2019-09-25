package com.example.launchertest

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.preference.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
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
    val inputStreamstr = InputStreamReader(context.openFileInput(file))
    println(inputStreamstr.readText())
    inputStreamstr.close()
}
