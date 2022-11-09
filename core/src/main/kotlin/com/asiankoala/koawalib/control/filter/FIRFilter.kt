package com.asiankoala.koawalib.control.filter

import java.util.LinkedList
import java.util.Queue

open class FIRFilter(
    private val bn: List<Double>
) {
    private val inputs: Queue<Double> = LinkedList()
    fun update(input: Double): Double {
        inputs.add(input)
        if (inputs.size > bn.size) inputs.remove()
        return inputs.zip(bn).sumOf { it.first * it.second }
    }
}
