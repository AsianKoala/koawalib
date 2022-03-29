package com.asiankoala.koawalib

import com.asiankoala.koawalib.math.absMax
import kotlin.math.absoluteValue
import kotlin.math.sign

object MaxComponentTest {
    @JvmStatic
    fun main(args: Array<String>) {
        var xPower = 0.004
        var yPower = 0.001
        var turnPower = -0.05

        val powers = mutableListOf(xPower, yPower, turnPower)
        val maxIdx = powers.indices.maxByOrNull { powers[it].absoluteValue }!!
        val highestPower = powers[maxIdx]
        powers[maxIdx] = absMax(0.1 * highestPower.sign, highestPower)
        xPower = powers[0]
        yPower = powers[1]
        turnPower = powers[2]

        println("$xPower, $yPower, $turnPower")
    }
}