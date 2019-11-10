package com.secretingradient.ingradientlauncher.data

import android.content.Context
import com.secretingradient.ingradientlauncher.BuildConfig
import java.io.FileNotFoundException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

open class FileDatasetList <D: Serializable> (val context: Context, val fileName: String) {
    open val rawDataset: MutableList<D> = getFileData() ?: mutableListOf()

    fun dumpData() {
        ObjectOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE)).use { it.writeObject(rawDataset) }
    }

    fun getFileData(): MutableList<D>? {
        var stream: ObjectInputStream? = null
        var loaded: MutableList<D>? = null
        try {
            stream = ObjectInputStream(context.openFileInput(fileName))
            loaded = stream.readObject() as? MutableList<D>
        } catch (e: FileNotFoundException) {
            if (BuildConfig.DEBUG) println("file not found ${context.filesDir.absolutePath}/$fileName")
        }
        stream?.close()

        return loaded
    }

    open fun loadData() {
        val data = getFileData()
        if (data != null) {
            rawDataset.clear()
            rawDataset.addAll(data)
        }
    }

    open fun clearData() {
        rawDataset.clear()
    }

    fun deleteFile() {
        clearData()
        context.deleteFile(fileName)
    }
}