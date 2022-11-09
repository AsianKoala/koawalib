package com.asiankoala.koawalib.control.filter

import java.util.*
import kotlin.math.pow

class ExponentialMovingAverageFilter(
    private val n: Int,
    private val alpha: Double = 2.0 / (n + 1.0)
) {
    private val inputs: Queue<Double> = LinkedList()

    private fun update(input: Double): Double {
        inputs.add(input)
        if (inputs.size > n + 1) inputs.remove()
        val sum = inputs.zip(List(n) { (1.0 - alpha).pow(it) }).sumOf { it.first * it.second }
        val weight = alpha * List(n) { (1.0 - alpha).pow(n + it) }.sum()
        return sum / weight
    }
}
