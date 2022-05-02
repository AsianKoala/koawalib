// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package com.asiankoala.koawalib.wpilib

import com.asiankoala.koawalib.wpilib.Numbers.N1
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.require

class LinearSystem<States : Num, Inputs : Num, Outputs : Num>(
    a: Matrix<States, States>,
    b: Matrix<States, Inputs>,
    c: Matrix<Outputs, States>,
    d: Matrix<Outputs, Inputs>
) {
    /** Continuous system matrix.  */
    private val m_A: Matrix<States, States>

    /** Continuous input matrix.  */
    private val m_B: Matrix<States, Inputs>

    /** Output matrix.  */
    private val m_C: Matrix<Outputs, States>

    /** Feedthrough matrix.  */
    private val m_D: Matrix<Outputs, Inputs>

    /**
     * Returns the system matrix A.
     *
     * @return the system matrix A.
     */
    val a: Matrix<States, States>
        get() = m_A

    /**
     * Returns an element of the system matrix A.
     *
     * @param row Row of A.
     * @param col Column of A.
     * @return the system matrix A at (i, j).
     */
    fun getA(row: Int, col: Int): kotlin.Double {
        return m_A.get(row, col)
    }

    /**
     * Returns the input matrix B.
     *
     * @return the input matrix B.
     */
    val b: Matrix<States, Inputs>
        get() = m_B

    /**
     * Returns an element of the input matrix B.
     *
     * @param row Row of B.
     * @param col Column of B.
     * @return The value of the input matrix B at (i, j).
     */
    fun getB(row: Int, col: Int): kotlin.Double {
        return m_B.get(row, col)
    }

    /**
     * Returns the output matrix C.
     *
     * @return Output matrix C.
     */
    val c: Matrix<Outputs, States>
        get() = m_C

    /**
     * Returns an element of the output matrix C.
     *
     * @param row Row of C.
     * @param col Column of C.
     * @return the double value of C at the given position.
     */
    fun getC(row: Int, col: Int): kotlin.Double {
        return m_C.get(row, col)
    }

    /**
     * Returns the feedthrough matrix D.
     *
     * @return the feedthrough matrix D.
     */
    val d: Matrix<Outputs, Inputs>
        get() = m_D

    /**
     * Returns an element of the feedthrough matrix D.
     *
     * @param row Row of D.
     * @param col Column of D.
     * @return The feedthrough matrix D at (i, j).
     */
    fun getD(row: Int, col: Int): kotlin.Double {
        return m_D.get(row, col)
    }

    /**
     * Computes the new x given the old x and the control input.
     *
     *
     * This is used by state observers directly to run updates based on state estimate.
     *
     * @param x The current state.
     * @param clampedU The control input.
     * @param dtSeconds Timestep for model update.
     * @return the updated x.
     */
    fun calculateX(
        x: Matrix<States, N1?>?, clampedU: Matrix<Inputs, N1?>?, dtSeconds: kotlin.Double
    ): Matrix<States, N1> {
        val discABpair: Unit = Discretization.discretizeAB(m_A, m_B, dtSeconds)
        return discABpair.getFirst().times(x).plus(discABpair.getSecond().times(clampedU))
    }

    /**
     * Computes the new y given the control input.
     *
     *
     * This is used by state observers directly to run updates based on state estimate.
     *
     * @param x The current state.
     * @param clampedU The control input.
     * @return the updated output matrix Y.
     */
    fun calculateY(x: Matrix<States, N1?>?, clampedU: Matrix<Inputs, N1?>?): Matrix<Outputs, N1> {
        return m_C.times(x).plus(m_D.times(clampedU))
    }

    override fun toString(): String {
        return java.lang.String.format(
            "Linear System: A\n%s\n\nB:\n%s\n\nC:\n%s\n\nD:\n%s\n",
            m_A.toString(), m_B.toString(), m_C.toString(), m_D.toString()
        )
    }

    /**
     * Construct a new LinearSystem from the four system matrices.
     *
     * @param a The system matrix A.
     * @param b The input matrix B.
     * @param c The output matrix C.
     * @param d The feedthrough matrix D.
     * @throws IllegalArgumentException if any matrix element isn't finite.
     */
    init {
        for (row in 0 until a.getNumRows()) {
            for (col in 0 until a.getNumCols()) {
                require(
                    Double.isFinite(
                        a.get(
                            row,
                            col
                        )
                    )
                ) { "Elements of A aren't finite. This is usually due to model implementation errors." }
            }
        }
        for (row in 0 until b.getNumRows()) {
            for (col in 0 until b.getNumCols()) {
                require(
                    Double.isFinite(
                        b.get(
                            row,
                            col
                        )
                    )
                ) { "Elements of B aren't finite. This is usually due to model implementation errors." }
            }
        }
        for (row in 0 until c.getNumRows()) {
            for (col in 0 until c.getNumCols()) {
                require(
                    Double.isFinite(
                        c.get(
                            row,
                            col
                        )
                    )
                ) { "Elements of C aren't finite. This is usually due to model implementation errors." }
            }
        }
        for (row in 0 until d.getNumRows()) {
            for (col in 0 until d.getNumCols()) {
                require(
                    Double.isFinite(
                        d.get(
                            row,
                            col
                        )
                    )
                ) { "Elements of D aren't finite. This is usually due to model implementation errors." }
            }
        }
        m_A = a
        m_B = b
        m_C = c
        m_D = d
    }
}