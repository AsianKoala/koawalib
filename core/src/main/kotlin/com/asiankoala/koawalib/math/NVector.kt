package com.asiankoala.koawalib.math

import kotlin.math.max
import kotlin.math.sqrt

/**
 * Represents N dimensional vectors
 * Pretty useless but it was fun to write
 */
class NVector(
    private val elems: List<Double>
) {
    constructor(vararg elems: Double) : this(elems.toList())
    constructor(n: Int, init: (Int) -> Double) : this(List(n, init))
    constructor(n: Int) : this(List(n) { 0.0 })

    val n = elems.size
    val norm = sqrt(elems.sumOf { it * it })
    val unit = this / norm
    val as2dVec = (this restrict 2).let { Vector(it[0], it[1]) }
    val asPose = (this restrict 3).let { Pose(it[0], it[1], it[2]) }

    private fun zipOp(other: NVector, op: (Pair<Double, Double>) -> Double): NVector {
        require(n == other.n)
        return NVector(elems.zip(other.elems).map(op))
    }

    infix fun dot(other: NVector) = zipOp(other) { it.first * it.second }.elems.sum()
    infix fun dist(other: NVector) = (this - other).norm
    infix fun push(elem: Double) = NVector(elems + elem)
    infix fun push(other: NVector) = NVector(elems + other.elems)
    infix fun pop(m: Int) = NVector(elems.dropLast(m))
    infix fun restrict(m: Int) = this pop max(0, n - m)
    infix fun map(op: (Double) -> Double) = NVector(elems.map(op))
    infix fun mapIndexed(op: (Int, Double) -> Double) = NVector(elems.mapIndexed(op))

    operator fun plus(other: NVector) = zipOp(other) { it.first + it.second }
    operator fun minus(other: NVector) = zipOp(other) { it.first - it.second }
    operator fun times(scalar: Double) = NVector(elems.map { it * scalar })
    operator fun div(scalar: Double) = NVector(elems.map { it / scalar })
    operator fun get(n: Int) = elems[n]
}
