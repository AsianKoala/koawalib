package com.asiankoala.koawalib.control.filter

class BoxcarFilter(N: Int) : FIRFilter(
    List(N) { 1.0 / (N + 1.0) }
)