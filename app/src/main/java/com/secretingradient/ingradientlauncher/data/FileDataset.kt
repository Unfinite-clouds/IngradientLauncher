package com.secretingradient.ingradientlauncher.data

import android.content.Context
import com.secretingradient.ingradientlauncher.BuildConfig
import java.io.FileNotFoundException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import com.secretingradient.ingradientlauncher.data.Data

open class FileDataset <I, D: Serializable> (val context: Context, val fileName: String){
    open val rawDataset: MutableMap<I, D> = getFileData() ?: mutableMapOf()

    fun dumpFileData() {
        ObjectOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE)).use { it.writeObject(rawDataset) }
    }

    fun getFileData(): MutableMap<I, D>? {
        var stream: ObjectInputStream? = null
        var loaded: MutableMap<I, D>? = null
        try {
            stream = ObjectInputStream(context.openFileInput(fileName))
            loaded = stream.readObject() as? MutableMap<I, D>
        } catch (e: FileNotFoundException) {
            if (BuildConfig.DEBUG) println("file not found ${context.filesDir.absolutePath}/$fileName")
        }
        stream?.close()

        return loaded
    }

    open fun loadFileData() {
        val data = getFileData()
        if (data != null) {
            rawDataset.clear()
            rawDataset.putAll(data)
        }
    }

    fun deleteFile() {
        context.deleteFile(fileName)
    }
}