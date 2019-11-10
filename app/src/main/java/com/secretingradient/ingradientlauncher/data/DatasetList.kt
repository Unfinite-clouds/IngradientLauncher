package com.secretingradient.ingradientlauncher.data

import com.secretingradient.ingradientlauncher.swap

class DatasetList<D: Data, I: Info>(val dataKeeper: DataKeeper, fileName: String) : FileDatasetList<D>(dataKeeper.context, fileName), MutableIterable<I> {

    override fun iterator(): MutableIterator<I> {
        return dataset.iterator()
    }

    private val dataset: MutableList<I> = mutableListOf()

    init {
        reloadDataset()
    }

    private fun reloadDataset() {
        dataset.clear()
        rawDataset.forEachIndexed {i, data ->
            dataset.add(i, data.createInfo(dataKeeper) as I)
        }
    }

    fun add(index: Int, info: I, dump: Boolean = true) {
        dataset.add(index, info)
        rawDataset.add(index, info.createData(index) as D)
        if (dump)
            dumpData()
    }

    fun set(index: Int, info: I, dump: Boolean = true) {
        dataset[index] = info
        rawDataset[index] = info.createData(index) as D
        if (dump)
            dumpData()
    }

    fun moveStack(from: Int, to: Int, dump: Boolean = true) {
        val tmp = dataset[from]
        val direction = if (to > from) 1 else -1
        val range = if (to > from) from until to else from downTo to
        for (i in range) {
            dataset[i] = dataset[i + direction]
            rawDataset[i] = rawDataset[i + direction]
        }
        set(to, tmp, dump)
    }

    fun swap(index1: Int, index2: Int, dump: Boolean = true) {
        dataset.swap(index1, index2)
        rawDataset.swap(index1, index2)
        if (dump)
            dumpData()
    }

    fun remove(index: Int, dump: Boolean = true) {
        dataset.removeAt(index)
        rawDataset.removeAt(index)
        if (dump)
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

    operator fun get(index: Int): I = dataset[index]

    val size: Int
        get() = dataset.size
}