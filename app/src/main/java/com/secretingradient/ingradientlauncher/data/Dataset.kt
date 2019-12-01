package com.secretingradient.ingradientlauncher.data

import com.secretingradient.ingradientlauncher.LauncherException
import com.secretingradient.ingradientlauncher.className
import com.secretingradient.ingradientlauncher.move
import com.secretingradient.ingradientlauncher.swap

class Dataset<D: Data, I: Info>(val dataKeeper: DataKeeper, fileName: String) : FileDataset<Int, D>(dataKeeper.context, fileName), MutableIterable<MutableMap.MutableEntry<Int, I>> {
    val debugAllowSave = true

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, I>> {
        return dataset.iterator()
    }

    private val dataset: MutableMap<Int, I> = mutableMapOf()

    init {
        reloadDataset()
    }

    private fun reloadDataset() {
        dataset.clear()
        rawDataset.forEach {
            dataset[it.key] = it.value.createInfo(dataKeeper) as I
        }
    }

    fun put(index: Int, info: I, replace: Boolean = false, dump: Boolean = true) {
        println("put $index, ${info.className()}, r = $replace")
        if (!replace && (dataset[index] != null || rawDataset[index] != null))
            throw LauncherException("item at index $index is busy. use replace=true to rewrite")
        dataset[index] = info
        rawDataset[index] = info.createData(index) as D
        if (dump && debugAllowSave)
            dumpData()
    }

    fun moveStack(from: Int, to: Int, dump: Boolean = true) {
        val tmp = dataset[from]
        val direction = if (to > from) 1 else -1
        val range = if (to > from) from until to else from downTo to - direction
        for (i in range) {
            println(i)
            dataset[i] = dataset[i + direction]!!
            rawDataset[i+1] = rawDataset[i + direction]!!
        }
        put(to, tmp as I, true, dump)
    }

    fun move(from: Int, to: Int, dump: Boolean = true) {
        println("$from -> $to")
        dataset.move(from, to)
        rawDataset.move(from, to)
        if (dump && debugAllowSave)
            dumpData()
    }

    fun swap(index1: Int, index2: Int, dump: Boolean = true) {
        dataset.swap(index1, index2)
        rawDataset.swap(index1, index2)
        if (dump && debugAllowSave)
            dumpData()
    }

    fun remove(index: Int, dump: Boolean = true) {
        println("remove $index")
        dataset.remove(index)
        rawDataset.remove(index)
        if (dump && debugAllowSave)
            dumpData()
    }

    override fun loadData() {
        super.loadData()
        reloadDataset()
    }

    override fun clearData() {
        super.clearData()
        dataset.clear()
    }

    operator fun get(index: Int): I? = dataset[index]

    val size: Int
        get() = dataset.size
}

