package com.asiankoala.koawalib.util

import java.util.*

internal infix fun <E> Set<E>.disjoint(other: Set<E>): Boolean = Collections.disjoint(this, other)
internal inline fun <T, R> Iterable<T>.containsBy(mapper: (T) -> R, key: R): Boolean {
    return this.map(mapper).contains(key)
}
