package com.asiankoala.koawalib.hardware.sensor

/**
 * Represents signs of x,y,z axes on IMU
 */
enum class AxesSigns(@JvmField val bVal: Int) {
    PPP(0),
    PPN(1),
    PNP(2),
    PNN(3),
    NPP(4),
    NPN(5),
    NNP(6),
    NNN(7)
}
