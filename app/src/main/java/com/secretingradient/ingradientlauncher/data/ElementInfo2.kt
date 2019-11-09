package com.secretingradient.ingradientlauncher.data

abstract class ElementInfo : Info {
    val info: Info
    var position = -1
    var snapWidth = -1
    var snapHeight = -1

    constructor(info: Info) {
        this.info = info
    }
    constructor(info: Info, pos: Int, snapW: Int, snapH: Int) {
        this.info = info
        position = pos
        snapWidth = snapW
        snapHeight = snapH
    }
}

