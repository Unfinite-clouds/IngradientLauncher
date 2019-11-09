package com.secretingradient.launchertest.data

import com.secretingradient.launchertest.AppView
import com.secretingradient.launchertest.swap

class DataHelper(val dataKeeper: DataKeeper, fileName: String) : FileDataset<Int, AppData>(dataKeeper.context, fileName) {
    private val dataset: MutableMap<Int, AppView.AppInfo> = mutableMapOf()

    init {
        reloadDataset()
    }

    private fun reloadDataset() {
        dataset.clear()
        rawDataset.forEach {
            dataset[it.key] = it.value.extract(dataKeeper)
        }
    }

    fun insert(index: Int, info: AppView.AppInfo, dump: Boolean = false) {
        dataset[index] = info
        rawDataset[index] = AppData(index, info.id)
        if (dump)
            dumpFileData()
    }

    fun move(from: Int, to: Int, dump: Boolean = false) {
        dataset.swap(from, to)
        rawDataset.swap(from, to)
        if (dump)
            dumpFileData()
    }

    override fun loadFileData() {
        super.loadFileData()
        reloadDataset()
    }

    operator fun get(index: Int): AppView.AppInfo = dataset[index]!!

    val size: Int
        get() = dataset.size
}

