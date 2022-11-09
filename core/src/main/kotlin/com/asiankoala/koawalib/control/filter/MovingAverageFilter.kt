package com.asiankoala.koawalib.control.filter

class MovingAverageFilter(N: Int) : FIRFilter(
    List(N) { 1.0 / (N + 1.0) }
)