package com.asiankoala.koawalib.util.internal

import java.util.*

internal infix fun <E> Collection<E>.disjoint(other: Collection<E>): Boolean = Collections.disjoint(this, other)
internal inline fun <T, R> Iterable<T>.containsBy(mapper: (T) -> R, key: R): Boolean {
    return this.map(mapper).contains(key)
}
