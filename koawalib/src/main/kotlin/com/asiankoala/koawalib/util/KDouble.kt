package com.asiankoala.koawalib.util

fun interface KDouble {
    fun invokeDouble(): Double

    val inverted: KDouble get() = KDouble { invokeDouble() * -1.0 }
}
