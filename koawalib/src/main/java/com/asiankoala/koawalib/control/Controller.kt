package com.asiankoala.koawalib.control

abstract class Controller {
    internal var output = 0.0

    abstract fun update(): Double
}
