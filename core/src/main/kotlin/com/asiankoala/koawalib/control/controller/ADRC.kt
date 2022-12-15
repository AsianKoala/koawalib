package com.asiankoala.koawalib.control.controller

import com.asiankoala.koawalib.math.clamp
import org.ejml.simple.SimpleMatrix
import kotlin.math.exp
import kotlin.math.pow

/**
 *
 */
class ADRC(
    delta: Double,
    private val b0: Double,
    tSettle: Double,
    kESO: Double,
    private val duConstraint: Double,
    halfGains: Pair<Boolean, Boolean> = Pair(false, false),
    private val uConstraint: Double = 1.0,
) {
    private val A: SimpleMatrix
    private val B: SimpleMatrix
    private val C: SimpleMatrix
    private val W: SimpleMatrix
    private val Ad: SimpleMatrix
    private val Bd: SimpleMatrix
    private val Ld: SimpleMatrix
    private var xHat: SimpleMatrix
    private val kP: Double
    private val kD: Double
    private var ukm1 = 0.0

    private fun limit(u: Double): Double {
        val deltaU = clamp(u - ukm1, -duConstraint, duConstraint)
        ukm1 = clamp(deltaU + ukm1, -uConstraint, uConstraint)
        return ukm1
    }

    // given by the equation x_hat[k+1] = A_d * x_hat[k] + B_d * u[k] + L_d * (y[k] - y_hat[k])
    private fun updateLuenBergerObserver(y: Double, ukm1: Double) {
        xHat = Ad.mult(xHat) + Bd.scale(ukm1) + Ld.scale(y)
    }

    fun update(y: Double, inp: Double, r: Double): Double {
        var u = inp
        updateLuenBergerObserver(y, u)
        u = (kP / b0) * r - W.transpose().mult(xHat)[0]
        u = limit(u)
        return u
    }

    init {
        A = SimpleMatrix(
            3, 3, true,
            doubleArrayOf(
                1.0, delta, (delta * delta) / 3.0,
                0.0, 1.0, delta,
                0.0, 0.0, 1.0
            )
        )

        B = SimpleMatrix(
            3, 1, true,
            doubleArrayOf(
                b0 * delta * delta / 2.0,
                b0 * delta,
                0.0
            )
        )

        C = SimpleMatrix(
            1, 3, true,
            doubleArrayOf(
                1.0, 0.0, 0.0
            )
        )

        val sCL = -6.0 / tSettle
        kP = sCL * sCL
        kD = -2.0 * sCL
        val sESO = kESO * sCL
        val zESO = exp(sESO * delta)

        Ld = SimpleMatrix(
            3, 1, true,
            doubleArrayOf(
                1.0 - zESO.pow(3),
                (3.0 / (2.0 * delta)) * (1 - zESO).pow(2) * (1.0 + zESO),
                (1.0 / delta.pow(2)) * (1.0 - zESO).pow(3)
            )
        ).scale(if(halfGains.first) 0.5 else 1.0)

        W = SimpleMatrix(
            3, 1, true,
            doubleArrayOf(
                kP / b0,
                kD / b0,
                1.0 / b0
            )
        ).scale(if(halfGains.second) 0.5 else 1.0)

        xHat = SimpleMatrix(3, 1)

        Ad = A - Ld.mult(C).mult(A)
        Bd = B - Ld.mult(C).mult(B)
    }
}
