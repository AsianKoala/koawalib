// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package edu.wpi.first.math.controller

import com.asiankoala.koawalib.wpilib.Discretization.discretizeAB
import com.asiankoala.koawalib.wpilib.Num
import com.asiankoala.koawalib.wpilib.Numbers.N1
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.system.LinearSystem
import org.ejml.simple.SimpleMatrix

/**
 * Constructs a plant inversion model-based feedforward from a [LinearSystem].
 *
 *
 * The feedforward is calculated as ** u_ff = B<sup>+</sup> (r_k+1 - A r_k) **,
 * where ** B<sup>+</sup> ** is the pseudoinverse of B.
 *
 *
 * For more on the underlying math, read
 * https://file.tavsys.net/control/controls-engineering-in-frc.pdf.
 */
class LinearPlantInversionFeedforward<States : Num?, Inputs : Num?, Outputs : Num?>(
    A: Matrix<States, States>?, B: Matrix<States, Inputs>, dtSeconds: Double
) {
    /** The current reference state.  */
    private var m_r: Matrix<States, N1>

    /** The computed feedforward.  */
    private var m_uff: Matrix<Inputs, N1>
    private val m_B: Matrix<States, Inputs>
    private val m_A: Matrix<States, States>

    /**
     * Constructs a feedforward with the given plant.
     *
     * @param plant The plant being controlled.
     * @param dtSeconds Discretization timestep.
     */
    constructor(
        plant: LinearSystem<States, Inputs, Outputs>, dtSeconds: Double
    ) : this(plant.getA(), plant.getB(), dtSeconds) {
    }

    /**
     * Returns the previously calculated feedforward as an input vector.
     *
     * @return The calculated feedforward.
     */
    val uff: Matrix<Inputs, N1>
        get() = m_uff

    /**
     * Returns an element of the previously calculated feedforward.
     *
     * @param row Row of uff.
     * @return The row of the calculated feedforward.
     */
    fun getUff(row: Int): Double {
        return m_uff.get(row, 0)
    }

    /**
     * Returns the current reference vector r.
     *
     * @return The current reference vector.
     */
    val r: Matrix<States, N1>
        get() = m_r

    /**
     * Returns an element of the current reference vector r.
     *
     * @param row Row of r.
     * @return The row of the current reference vector.
     */
    fun getR(row: Int): Double {
        return m_r.get(row, 0)
    }

    /**
     * Resets the feedforward with a specified initial state vector.
     *
     * @param initialState The initial state vector.
     */
    fun reset(initialState: Matrix<States, N1>) {
        m_r = initialState
        m_uff.fill(0.0)
    }

    /** Resets the feedforward with a zero initial state vector.  */
    fun reset() {
        m_r.fill(0.0)
        m_uff.fill(0.0)
    }

    /**
     * Calculate the feedforward with only the desired future reference. This uses the internally
     * stored "current" reference.
     *
     *
     * If this method is used the initial state of the system is the one set using [ ][LinearPlantInversionFeedforward.reset]. If the initial state is not set it defaults to
     * a zero vector.
     *
     * @param nextR The reference state of the future timestep (k + dt).
     * @return The calculated feedforward.
     */
    fun calculate(nextR: Matrix<States, N1>): Matrix<Inputs, N1> {
        return calculate(m_r, nextR)
    }

    /**
     * Calculate the feedforward with current and future reference vectors.
     *
     * @param r The reference state of the current timestep (k).
     * @param nextR The reference state of the future timestep (k + dt).
     * @return The calculated feedforward.
     */
    fun calculate(r: Matrix<States, N1>?, nextR: Matrix<States, N1>): Matrix<Inputs, N1> {
        m_uff = Matrix(m_B.solve(nextR.minus(m_A.times(r))))
        m_r = nextR
        return m_uff
    }

    /**
     * Constructs a feedforward with the given coefficients.
     *
     * @param A Continuous system matrix of the plant being controlled.
     * @param B Continuous input matrix of the plant being controlled.
     * @param dtSeconds Discretization timestep.
     */
    init {
        val (first, second) = discretizeAB<Num, Num>(A, B, dtSeconds)
        m_A = first
        m_B = second
        m_r = Matrix(SimpleMatrix(B.getNumRows(), 1))
        m_uff = Matrix(SimpleMatrix(B.getNumCols(), 1))
        reset()
    }
}