package com.asiankoala.koawalib.control.controller

data class ADRCConfig(
    val delta: Double,
    val b0: Double,
    val tSettle: Double,
    val kESO: Double,
    val duConstraint: Double,
    val halfGains: Pair<Boolean, Boolean>,
    val uConstraint: Double
)
