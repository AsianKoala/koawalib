package com.asiankoala.koawalib.control.feedforward

class ElevatorFeedforward(
    private val kg: Double,
    coefficients: FeedforwardCoefficients,
    kStatic: Double
) : MotorFeedforward(coefficients, kStatic) {
    override fun getFeedforward(x: Double, v: Double, a: Double): Double {
        return v * coefficients.kv + a * coefficients.ka + kg
    }
}
