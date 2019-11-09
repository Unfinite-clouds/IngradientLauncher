package com.secretingradient.ingradientlauncher.data

class AppState(override val data: AppData, override val info: Info): State() {
    override var datasetPosition: Int
        get() = position
        set(value) { position = value }
    var position
        get() = data.position
        set(value) { data.position = value }
}