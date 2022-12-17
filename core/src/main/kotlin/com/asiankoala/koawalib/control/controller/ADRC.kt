package com.asiankoala.koawalib.control.controller

import com.asiankoala.koawalib.math.clamp
import org.ejml.simple.SimpleMatrix
import kotlin.math.exp
import kotlin.math.pow

/**
 *
 */
class ADRC(
    private val config: ADRCConfig
) {
    private val A: SimpleMatrix = SimpleMatrix(
        3, 3, true,
        doubleArrayOf(
            1.0, config.delta, config.delta.pow(2) / 3.0,
            0.0, 1.0, config.delta,
            0.0, 0.0, 1.0
        )
    )
    private val B: SimpleMatrix = SimpleMatrix(
        3, 1, true,
        doubleArrayOf(
            config.b0 * config.delta.pow(2) / 2.0,
            config.b0 * config.delta,
            0.0
        )
    )
    private val C: SimpleMatrix = SimpleMatrix(
        1, 3, true,
        doubleArrayOf(
            1.0, 0.0, 0.0
        )
    )
    private val W: SimpleMatrix
    private val Ad: SimpleMatrix
    private val Bd: SimpleMatrix
    private val Ld: SimpleMatrix
    private var xHat: SimpleMatrix
    private val kP: Double
    private val kD: Double

    private fun limit(u: Double, ukm1: Double): Double {
        val deltaU = clamp(u - ukm1, -config.duConstraint, config.duConstraint)
        ukm1 = clamp(deltaU + ukm1, -config.uConstraint, config.uConstraint)
        return ukm1
    }

    // given by the equation x_hat[k+1] = A_d * x_hat[k] + B_d * u[k] + L_d * (y[k] - y_hat[k])
    private fun updateLuenBergerObserver(y: Double, ukm1: Double) {
        xHat = Ad.mult(xHat) + Bd.scale(ukm1) + Ld.scale(y)
    }

    fun update(y: Double, ukm1: Double, r: Double): Double {
        updateLuenBergerObserver(y, ukm1)
        var u = (kP / config.b0) * r - W.transpose().mult(xHat)[0]
        u = limit(u, ukm1)
        return u
    }

    init {
        val sCL = -6.0 / config.tSettle
        kP = sCL * sCL
        kD = -2.0 * sCL
        val sESO = config.kESO * sCL
        val zESO = exp(sESO * config.delta)

        Ld = SimpleMatrix(
            3, 1, true,
            doubleArrayOf(
                1.0 - zESO.pow(3),
                (3.0 / (2.0 * config.delta)) * (1 - zESO).pow(2) * (1.0 + zESO),
                (1.0 / config.delta.pow(2)) * (1.0 - zESO).pow(3)
            )
        ).scale(if (config.halfGains.first) 0.5 else 1.0)

        W = SimpleMatrix(
            3, 1, true,
            doubleArrayOf(
                kP / config.b0,
                kD / config.b0,
                1.0 / config.b0
            )
        ).scale(if (config.halfGains.second) 0.5 else 1.0)

        xHat = SimpleMatrix(3, 1)

        Ad = A - Ld.mult(C).mult(A)
        Bd = B - Ld.mult(C).mult(B)
    }
}
