package com.secretingradient.launchertest.data

import android.content.Context
import com.secretingradient.launchertest.BuildConfig
import java.io.FileNotFoundException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

open class FileDataset <Index, Data: Serializable> (val context: Context, val fileName: String) {
    open val rawDataset: MutableMap<Index, Data> = getFileData() ?: mutableMapOf()

    fun dumpFileData() {
        ObjectOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE)).use { it.writeObject(rawDataset) }
    }

    fun getFileData(): MutableMap<Index, Data>? {
        var stream: ObjectInputStream? = null
        var loaded: MutableMap<Index, Data>? = null
        try {
            stream = ObjectInputStream(context.openFileInput(fileName))
            loaded = stream.readObject() as? MutableMap<Index, Data>
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