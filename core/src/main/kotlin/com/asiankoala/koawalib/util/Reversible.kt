package com.asiankoala.koawalib.util

data class Reversible<T>(
    @JvmField var value: T,
    @JvmField var revValue: T
) {
    operator fun get(reversed: Boolean): T {
        return if (reversed) revValue else value
    }
}
