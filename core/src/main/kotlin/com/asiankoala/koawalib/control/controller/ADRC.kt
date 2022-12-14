package com.asiankoala.koawalib.control.controller

import com.asiankoala.koawalib.util.Clock
import org.ejml.simple.SimpleMatrix
import kotlin.math.exp
import kotlin.math.pow

class ADRC(
    private val delta: Double,
    private val b0: Double,
    tSettle: Double,
    kESO: Double,
    halfGains: Pair<Boolean, Boolean>,
) {
    private val A: SimpleMatrix
    private val B: SimpleMatrix
    private val C: SimpleMatrix
    private val L: SimpleMatrix
    private val W: SimpleMatrix
    private val OA: SimpleMatrix
    private val OB: SimpleMatrix
    private val OC: SimpleMatrix
    private var xHat: SimpleMatrix

    private val kP: Double
    private var ukm1 = 0.0
    private var lastTime: Double? = null
    private var lastOutput = 0.0

    private fun updateESO(y: Double, ukm1: Double) {
        xHat = OA.mult(xHat) + OB.scale(ukm1) + L.scale(y)
    }

    private fun limiter(uControl: Double): Double {
        val deltaU = uControl - ukm1
        ukm1 += deltaU
        return ukm1
    }

    fun call(y: Double, inp: Double, r: Double, zoh: Boolean = false): Double {
        val time = Clock.seconds
        val dt: Double
        var u = inp

        if(zoh) {
            dt = lastTime?.let { time - it } ?: 1e-10
            if(dt < delta) {
                return lastOutput
            }
        }

        updateESO(y, u)
        u = (kP / b0) * r - W.transpose().mult(xHat)[0]
        u = limiter(u)

        lastOutput = u
        lastTime = time
        return u
    }

    init {
        val nx = 2
        A = SimpleMatrix(
            2, 2, true,
            doubleArrayOf(
                1.0, delta,
                0.0, 1.0
            )
        )

        B = SimpleMatrix(
            1, 2, true,
            doubleArrayOf(
                b0 * delta, 0.0
            )
        )

        C = SimpleMatrix(
            2, 1, true,
            doubleArrayOf(
                1.0,
                0.0
            )
        )

        val sCL = -4.0 / tSettle
        kP = -2.0 * sCL

        val sESO = kESO * sCL
        val zESO = exp(sESO * delta)

        L = SimpleMatrix(
            2, 1, true,
            doubleArrayOf(
                1.0 - zESO * zESO,
                (1.0 / delta) * (1.0 - zESO).pow(2)
            )
        ).scale(if(halfGains.first) 0.5 else 1.0)

        W = SimpleMatrix(
            2, 1, true,
            doubleArrayOf(
                kP / b0,
                1.0 / b0
            )
        ).scale(if(halfGains.second) 0.5 else 1.0)

        xHat = SimpleMatrix(nx, 1)

        OA = A - L.mult(C).mult(A)
        OB = B - L.mult(C).mult(B)
        OC = C
    }
}