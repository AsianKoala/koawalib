package com.asiankoala.koawalib.util

object Clock {
    val seconds get() = milliseconds / 1000.0
    val milliseconds get() = System.nanoTime() / 1e6
}
