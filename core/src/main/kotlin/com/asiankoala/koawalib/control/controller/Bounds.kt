package com.asiankoala.koawalib.control.controller

data class Bounds(
    val lowerBound: Double? = null,
    val upperBound: Double? = null
) {
    val isBounded = lowerBound != null && upperBound != null
}
