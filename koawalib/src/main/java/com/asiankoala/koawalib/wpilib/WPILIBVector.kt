package com.asiankoala.koawalib.wpilib

import com.asiankoala.koawalib.math.d
import org.ejml.simple.SimpleMatrix

@Suppress("unused")
class WPILIBVector<R : Num> : Matrix<R, Numbers.N1> {
    constructor(rows: Nat<R>) : super(rows, Nat.N1())
    constructor(storage: SimpleMatrix) : super(storage)
    constructor(other: Matrix<R, Numbers.N1>) : super(other)

    override fun times(value: Double): WPILIBVector<R> {
        return WPILIBVector(storage.scale(value))
    }

    override fun div(value: Double): Matrix<R, Numbers.N1> {
        return WPILIBVector(storage.divide(value))
    }

    override fun div(value: Int): Matrix<R, Numbers.N1> {
        return WPILIBVector(storage.divide(value.d))
    }
}