package com.asiankoala.koawalib.wpilib

import org.ejml.simple.SimpleMatrix

data class LinearSystem(
    val A: SimpleMatrix,
    val B: SimpleMatrix,
    val C: SimpleMatrix,
    val D: SimpleMatrix
) {
    fun calculateY(x: SimpleMatrix, clampedU: SimpleMatrix): SimpleMatrix {
        return C.mult(x).plus(D.mult(clampedU))
    }

    init {
        val l = listOf(A, B, C, D)
        for (m in l) {
            for (r in 0..m.numRows()) {
                for (c in 0..m.numCols()) {
                    if (!m[r, c].isFinite()) throw Exception("matrix not finite")
                }
            }
        }
    }
}
