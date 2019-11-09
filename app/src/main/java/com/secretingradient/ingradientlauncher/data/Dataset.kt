package com.secretingradient.ingradientlauncher.data

import com.secretingradient.ingradientlauncher.LauncherException

class Dataset <D: Data, S: State> (val dataHolder: DataKeeper.DataHolder<Int, D>, dataKeeper: DataKeeper) {
    val dataset: MutableMap<Int, S>

    init {
        dataset = mutableMapOf()
        dataHolder.data.forEach {
            val d = it.value.extract(dataKeeper)
            dataset[d.datasetPosition]
        }
    }

    fun insertItem(item: S, replace: Boolean = false, dump: Boolean = true) {
        val index = item.datasetPosition

        if (!replace && dataset.containsKey(index))
            throw LauncherException("attempt to rewrite element ${dataset[index]} at index $index")

        dataset[index] = item

        if (dump)
            dataHolder.dumpData()
    }

    fun removeItem(index: Int, dump: Boolean = true) {
        dataset.remove(index)

        if (dump)
            dataHolder.dumpData()
    }

    fun moveItem(from: Int, to: Int, dump: Boolean = true) {
        if (from == to)
            return
        if (dataset.containsKey(to))
            throw LauncherException("attempt to rewrite element ${dataset[to]} at index $to")
        if (!dataset.containsKey(from))
            throw LauncherException("attempt to move null element at index $from")

        dataset[to] = dataset[from]!!
        dataset[to]!!.datasetPosition = to
        dataset.remove(from)

        if (dump)
            dataHolder.dumpData()
    }

    operator fun get(index: Int): S {
        return dataset[index]!!
    }
/*
    fun dumpData() {
        ObjectOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE)).use { it.writeObject(data) }
    }

    fun loadData() {
        var stream: ObjectInputStream? = null
        var loaded: MutableMap<Int, S>? = null
        try {
            stream = ObjectInputStream(context.openFileInput(fileName))
            loaded = stream.readObject() as? MutableMap<Int, S>
        } catch (e: FileNotFoundException) {
            if (BuildConfig.DEBUG) println("file not found ${context.filesDir.absolutePath}/$fileName")
        }
        data = loaded ?: mutableMapOf()

        stream?.close()
    }

    fun deleteFile() {
        context.deleteFile(fileName)
    }
*/
}