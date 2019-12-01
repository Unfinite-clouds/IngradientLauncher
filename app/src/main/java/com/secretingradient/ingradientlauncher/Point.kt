package com.secretingradient.ingradientlauncher

class Point {
    private val array = IntArray(2)
    var x
        get() = array[0]
        set(value) {array[0] = value}
    var y
        get() = array[1]
        set(value) {array[1] = value}
    fun set(x: Int, y: Int) {
        this.x = x
        this.y = y
    }
    fun offset(dx: Int, dy: Int) {
        x += dx
        y += dy
    }
    fun asArray(): IntArray {
        return array
    }
    override fun toString(): String {
        return "Point($x, $y)"
    }
    fun equals(x: Int, y: Int): Boolean {
        return this.x == x && this.y == y
    }
    override operator fun equals(other: Any?): Boolean {
        return when (other) {
            is Point -> equals(other.x, other.y)
            is android.graphics.Point -> equals(other.x, other.y)
            else -> false
        }
    }
    override fun hashCode(): Int {
        return 31*x+y
    }
}