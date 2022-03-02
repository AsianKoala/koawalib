package com.asiankoala.koawalib.control.feedforward

open class MotorFeedforward(protected val coefficients: FeedforwardCoefficients, override val kStatic: Double) :
    Feedforward {
    override fun getFeedforward(x: Double, v: Double, a: Double): Double {
        return coefficients.kv * v + coefficients.ka * a
    }
}
