package com.secretingradient.launchertest.data

import com.secretingradient.launchertest.swap

class Dataset<D: Data, I: Info>(val dataKeeper: DataKeeper, fileName: String) : FileDataset<Int, D>(dataKeeper.context, fileName) {
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
            dumpFileData()
    }

    fun move(from: Int, to: Int, dump: Boolean = true) {
        dataset.swap(from, to)
        rawDataset.swap(from, to)
        if (dump)
            dumpFileData()
    }

    fun remove(index: Int, dump: Boolean = true) {
        dataset.remove(index)
        rawDataset.remove(index)
        if (dump)
            dumpFileData()
    }

    override fun loadFileData() {
        super.loadFileData()
        reloadDataset()
    }

    operator fun get(index: Int): I = dataset[index]!!

    val size: Int
        get() = dataset.size
}
