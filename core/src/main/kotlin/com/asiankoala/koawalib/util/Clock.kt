package com.asiankoala.koawalib.util

/**
 * Standard clock utility
 * @property seconds System time in seconds
 * @property milliseconds System time in milliseconds
 */
object Clock {
    @JvmStatic val seconds get() = System.nanoTime() / 1e9
    @JvmStatic val milliseconds get() = System.nanoTime() / 1e6
}
