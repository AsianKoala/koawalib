package com.asiankoala.koawalib.wpilib

import org.ejml.simple.SimpleMatrix

open class MatBuilder<R : Num, C : Num>(val rows: Nat<R>, val cols: Nat<C>) {

    /**
     * Fills the matrix with the given data, encoded in row major form. (The matrix is filled row by
     * row, left to right with the given data).
     *
     * @param data The data to fill the matrix with.
     * @return The constructed matrix.
     */
    fun fill(vararg data: Double): Matrix<R, C> {
        return if (data.size != rows.getNum() * cols.getNum()) {
            throw IllegalArgumentException(
                "Invalid matrix data provided. Wanted ${rows.getNum()} x ${cols.getNum()} matrix, but got ${data.size} elements"
            )
        } else {
            Matrix(SimpleMatrix(rows.getNum(), cols.getNum(), true, data))
        }
    }
}
