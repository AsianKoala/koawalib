package com.asiankoala.koawalib.math

import kotlin.math.max
import kotlin.math.sqrt

/**
 * Represents N dimensional vectors
 */
class NVector(
    val elems: List<Double>
) {
    constructor(vararg elems: Double) : this(elems.toList())
    val n = elems.size
    val norm = sqrt(elems.sumOf { it * it })
    val unit = this / norm
    val as2dVec = this pop (max(0, n - 2))
    val asPose = this pop(max(0, n - 3))

    private fun zipOp(other: NVector, op: (Pair<Double, Double>) -> Double): NVector {
        require(n == other.n)
        return NVector(elems.zip(other.elems).map(op))
    }

    infix fun dot(other: NVector) = zipOp(other) { it.first * it.second }.elems.sum()
    infix fun dist(other: NVector) = (this - other).norm
    infix fun push(elem: Double) = NVector(elems + elem)
    infix fun push(other: NVector) = NVector(elems + other.elems)
    infix fun pop(n: Int) = NVector(elems.dropLast(n))
    operator fun plus(other: NVector) = zipOp(other) { it.first + it.second }
    operator fun minus(other: NVector) = zipOp(other) { it.first - it.second }
    operator fun times(scalar: Double) = NVector(elems.map { it * scalar })
    operator fun div(scalar: Double) = NVector(elems.map { it / scalar })
}