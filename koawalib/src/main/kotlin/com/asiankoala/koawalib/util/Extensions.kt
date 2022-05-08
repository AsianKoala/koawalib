package com.asiankoala.koawalib.util

import java.util.*

infix fun <E>Set<E>.disjoint(other: Set<E>): Boolean = Collections.disjoint(this, other)