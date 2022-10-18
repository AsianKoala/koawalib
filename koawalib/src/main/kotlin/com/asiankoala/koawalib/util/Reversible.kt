package com.asiankoala.koawalib.util

class Reversible<T>(
    private val value: T,
    private val revValue: T
) {
    operator fun get(reversed: Boolean): T {
        return if(reversed) revValue else value
    }
}