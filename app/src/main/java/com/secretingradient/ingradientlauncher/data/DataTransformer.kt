package com.secretingradient.ingradientlauncher.data

import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.element.AppView

abstract class DataTransformer <Index, Data, View> {
    abstract val datasetL: DataKeeper.OnDataChangedListener<Index, Data>

    protected abstract fun getIndexFromView(v: View): Index
    protected abstract fun getDataFromView(v: View): Data
    protected abstract fun inflateData(data: Data): View

    fun insertView(v: View) {
        onInsertView(v)
        notifyViewInserted(v)
    }

    protected abstract fun onInsertView(v: View)

    private fun notifyViewInserted(v: View) {
        val index = getIndexFromView(v)
        val data = getDataFromView(v)
        datasetL.onInserted(index, data)
    }

    fun insertData(data: Data) {
        onInsertData(data)
        notifyDataInserted(data)
    }

    protected abstract fun onInsertData(data: Data)

    private fun notifyDataInserted(data: Data) {
        onInsertView(inflateData(data))
    }
}

class DataTransformerAllInOne {
    private fun getIndexFromView(v: AppView): Int = (v.layoutParams as SnapLayout.SnapLayoutParams).position
    private fun getDataFromView(v: AppView): String = v.appInfo.id
    private fun inflateData(data: String): AppView =

    fun insertView(v: AppView) {
        onInsertView(v)
        notifyViewInserted(v)
    }

    fun insertData(data: String) {
        onInsertData(data)
        notifyDataInserted(data)
    }

     fun onInsertView(v: AppView)

    private fun notifyViewInserted(v: AppView) {
        val index = getIndexFromView(v)
        val data = getDataFromView(v)
        datasetL.onInserted(index, data)
    }


     fun onInsertData(data: String)

    private fun notifyDataInserted(data: String) {
        onInsertView(inflateData(data))
    }
}


// add it to AppView for use DataTransformer2 with AppView
abstract class DataCompatibleTest {
    abstract val v: AppView
    fun getIndex(): Int = (v.layoutParams as SnapLayout.SnapLayoutParams).position
    fun getData(): String = v.appInfo.id
}

interface DataCompatible <Index, Data> {
    fun getIndex(): Index
    fun getData(): Data
}

class DataTransformer2 <Index, Data, View: DataCompatible<Index, Data>> {
    private fun notifyViewInserted(v: View) {
        val index = v.getIndex()
        val data = v.getData()
        datasetL.onInserted(index, data)
    }
}