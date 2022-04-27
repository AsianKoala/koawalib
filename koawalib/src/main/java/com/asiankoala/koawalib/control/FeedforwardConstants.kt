package com.asiankoala.koawalib.control

/**
 * Standard feedforward constants
 * @param kV velocity ff
 * @param kA accel ff
 * @param kStatic static friction ff
 * @param kCos arm ff
 * @param kG gravity ff (lifts)
 * @param kF ff function with current position, velocity as arguments
 * @param kTargetF ff function with target position as argument
 */
data class FeedforwardConstants(
    val kV: Double = 0.0,
    val kA: Double = 0.0,
    val kStatic: Double = 0.0,
    val kCos: Double = 0.0,
    val kG: Double = 0.0,
    val kF: (Double, Double?) -> Double = { _, _ -> 0.0 },
    val kTargetF: (Double) -> Double = { _ -> 0.0 },
)
