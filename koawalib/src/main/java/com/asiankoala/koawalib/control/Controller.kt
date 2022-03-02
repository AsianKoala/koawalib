package com.asiankoala.koawalib.control

abstract class Controller {
    protected abstract fun process(): Double

    var output = 0.0
        protected set

    fun update() {
        output = process()
    }
}
