package com.secretingradient.ingradientlauncher.data

import android.content.Context
import com.secretingradient.ingradientlauncher.BuildConfig
import java.io.FileNotFoundException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class FileData <T> (val context: Context, val fileName: String, initLoad: Boolean = true, initData: T) {
    var data: T = if (initLoad) loadData() ?: initData else initData

    fun dumpData() {
        ObjectOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE)).use { it.writeObject(data) }
    }

    fun loadData(): T? {
        var stream: ObjectInputStream? = null
        var loaded: T? = null
        try {
            stream = ObjectInputStream(context.openFileInput(fileName))
            loaded = stream.readObject() as? T
        } catch (e: FileNotFoundException) {
            if (BuildConfig.DEBUG) println("file not found ${context.filesDir.absolutePath}/$fileName")
        }
        stream?.close()
        return loaded
    }

    fun deleteFile() {
        context.deleteFile(fileName)
    }
}