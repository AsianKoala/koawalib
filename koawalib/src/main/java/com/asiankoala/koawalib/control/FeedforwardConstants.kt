package com.asiankoala.koawalib.control

data class FeedforwardConstants(
    val kV: Double = 0.0,
    val kA: Double = 0.0,
    val kStatic: Double = 0.0,
    val kCos: Double = 0.0,
    val kG: Double = 0.0,
    val kF: (Double, Double?) -> Double = { _, _ -> 0.0 },
    val kTargetF: (Double) -> Double = { _ -> 0.0 },
)