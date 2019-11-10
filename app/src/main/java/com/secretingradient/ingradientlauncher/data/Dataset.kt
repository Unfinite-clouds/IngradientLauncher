package com.secretingradient.ingradientlauncher.data

import com.secretingradient.ingradientlauncher.move
import com.secretingradient.ingradientlauncher.swap

class Dataset<D: Data, I: Info>(val dataKeeper: DataKeeper, fileName: String) : FileDataset<Int, D>(dataKeeper.context, fileName), MutableIterable<MutableMap.MutableEntry<Int, I>> {

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

    fun insert(index: Int, info: I, dump: Boolean = true) {
        dataset[index] = info
        rawDataset[index] = info.createData(index) as D
        if (dump)
            dumpData()
    }

    fun move(from: Int, to: Int, dump: Boolean = true) {
        dataset.move(from, to)
        rawDataset.move(from, to)
        if (dump)
            dumpData()
    }

    fun swap(index1: Int, index2: Int, dump: Boolean = true) {
        dataset.swap(index1, index2)
        rawDataset.swap(index1, index2)
        if (dump)
            dumpData()
    }

    fun remove(index: Int, dump: Boolean = true) {
        dataset.remove(index)
        rawDataset.remove(index)
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

    operator fun get(index: Int): I = dataset[index]!!

    val size: Int
        get() = dataset.size
}
