package com.example.launchertest

import android.content.Context
import java.io.*

object Storable {
    data class FileInfo<T>(val fileName: String, val type: Class<T>)

    val TEST = FileInfo("test", List::class.java)
    val ALL_APPS = FileInfo("ALL_APPS", MutableMap::class.java)
    val CUSTOM_GRID_APPS = FileInfo("CUSTOM_GRID_APPS", MutableMap::class.java)
    val MAIN_SCREEN_APPS = FileInfo("MAIN_SCREEN_APPS", MutableList::class.java)

    fun load(inputStream: FileInputStream) : Any? {
        val loaded: Any?
        var objIn: ObjectInputStream? = null
        try {
            objIn = ObjectInputStream(inputStream)
            loaded = objIn.readObject()
        } finally {
            objIn?.close()
        }
        return loaded
    }

    inline fun <reified T> loadAuto(context: Context, what: FileInfo<T>): T? {
        val a: Any?
        var inputStream: InputStream? = null
        try {
            inputStream = context.openFileInput(what.fileName)
            a = load(inputStream)
        } catch (e: FileNotFoundException) {
            return null
        } finally {
            inputStream?.close()
        }
        return if (a is T) a else null
    }

    inline fun <reified T> dumpAuto(context: Context, what: Any, how: FileInfo<T>) {
        ObjectOutputStream(context.openFileOutput(how.fileName, Context.MODE_PRIVATE)).use { it.writeObject(what) }
    }

    fun <T> deleteFile(context: Context, file: FileInfo<T>) {
        context.deleteFile(file.fileName)
    }
}