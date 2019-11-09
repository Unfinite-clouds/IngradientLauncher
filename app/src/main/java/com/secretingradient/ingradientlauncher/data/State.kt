package com.secretingradient.ingradientlauncher.data

abstract class State {
    protected abstract val data: Data
    abstract var datasetPosition: Int
    abstract val info: Info
}
