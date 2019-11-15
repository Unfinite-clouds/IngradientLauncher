package com.secretingradient.ingradientlauncher

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText

class SearchDataView<DataType> : EditText {
    var listener: OnTextChangedListener? = null
    lateinit var data: List<DataType>
    lateinit var filteredData: List<DataType>
    lateinit var param: (obj: DataType) -> String
    var lastText = ""

    init {
        setSingleLine()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun init(data: List<DataType>, param: (obj: DataType) -> String, listener: OnTextChangedListener?) {
        this.data = data
        this.filteredData = data
        this.param = param
        this.listener = listener
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        if (!this::data.isInitialized)
            return

        val newText = text?.toString()
        filteredData = if (newText == null || newText == "") {
            data
        } else if (lastText != "" && newText.startsWith(lastText, true)) {
            // add symbol to end
            filteredData.filter { param(it).startsWith(newText,true) }
        } else {
            data.filter { param(it).startsWith(newText,true)}
        }
        lastText = newText ?: ""
        listener?.onTextChanged(newText)

    }

    interface OnTextChangedListener {
        fun onTextChanged(newText: String?)
    }
}