// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package edu.wpi.first.math.controller

import com.asiankoala.koawalib.wpilib.system.Discretization.discretizeAB
import com.asiankoala.koawalib.wpilib.Nat
import com.asiankoala.koawalib.wpilib.Num
import com.asiankoala.koawalib.wpilib.Numbers.N1
import edu.wpi.first.math.*
import edu.wpi.first.math.MathSharedStore.reportError
import edu.wpi.first.math.StateSpaceUtil.isStabilizable
import edu.wpi.first.math.StateSpaceUtil.makeCostMatrix
import edu.wpi.first.math.system.LinearSystem
import org.ejml.simple.SimpleMatrix

/**
 * Contains the controller coefficients and logic for a linear-quadratic regulator (LQR). LQRs use
 * the control law u = K(r - x).
 *
 *
 * For more on the underlying math, read
 * https://file.tavsys.net/control/controls-engineering-in-frc.pdf.
 */
class LinearQuadraticRegulator<States : Num?, Inputs : Num?, Outputs : Num?> {
    /** The current reference state.  */
    private var m_r: Matrix<States, N1>? = null

    /** The computed and capped controller output.  */
    private var m_u: Matrix<Inputs, N1>? = null

    // Controller gain.
    private var m_K: Matrix<Inputs, States>? = null

    /**
     * Constructs a controller with the given coefficients and plant. Rho is defaulted to 1.
     *
     * @param plant The plant being controlled.
     * @param qelms The maximum desired error tolerance for each state.
     * @param relms The maximum desired control effort for each input.
     * @param dtSeconds Discretization timestep.
     */
    constructor(
        plant: LinearSystem<States, Inputs, Outputs>,
        qelms: Vector<States>,
        relms: Vector<Inputs>,
        dtSeconds: Double
    ) : this(
        plant.getA(),
        plant.getB(),
        makeCostMatrix<Elements>(qelms),
        makeCostMatrix<Elements>(relms),
        dtSeconds
    ) {
    }

    /**
     * Constructs a controller with the given coefficients and plant.
     *
     * @param A Continuous system matrix of the plant being controlled.
     * @param B Continuous input matrix of the plant being controlled.
     * @param qelms The maximum desired error tolerance for each state.
     * @param relms The maximum desired control effort for each input.
     * @param dtSeconds Discretization timestep.
     */
    constructor(
        A: Matrix<States, States>?,
        B: Matrix<States, Inputs>?,
        qelms: Vector<States>,
        relms: Vector<Inputs>,
        dtSeconds: Double
    ) : this(
        A,
        B,
        makeCostMatrix<Elements>(qelms),
        makeCostMatrix<Elements>(relms),
        dtSeconds
    ) {
    }

    /**
     * Constructs a controller with the given coefficients and plant.
     *
     * @param A Continuous system matrix of the plant being controlled.
     * @param B Continuous input matrix of the plant being controlled.
     * @param Q The state cost matrix.
     * @param R The input cost matrix.
     * @param dtSeconds Discretization timestep.
     */
    constructor(
        A: Matrix<States, States>?,
        B: Matrix<States, Inputs>,
        Q: Matrix<States, States>,
        R: Matrix<Inputs, Inputs>,
        dtSeconds: Double
    ) {
        val (discA, discB) = discretizeAB<Num, Num>(A, B, dtSeconds)
        if (!isStabilizable<States, Inputs>(discA, discB)) {
            val builder = StringBuilder("The system passed to the LQR is uncontrollable!\n\nA =\n")
            builder
                .append(discA.storage.toString())
                .append("\nB =\n")
                .append(discB.storage.toString())
                .append('\n')
            val msg = builder.toString()
            reportError(msg, Thread.currentThread().stackTrace)
            throw IllegalArgumentException(msg)
        }
        val S: Unit = Drake.discreteAlgebraicRiccatiEquation(discA, discB, Q, R)

        // K = (BᵀSB + R)⁻¹BᵀSA
        m_K = discB
            .transpose()
            .times(S)
            .times(discB)
            .plus(R)
            .solve(discB.transpose().times(S).times(discA))
        m_r = Matrix(SimpleMatrix(B.getNumRows(), 1))
        m_u = Matrix(SimpleMatrix(B.getNumCols(), 1))
        reset()
    }

    /**
     * Constructs a controller with the given coefficients and plant.
     *
     * @param A Continuous system matrix of the plant being controlled.
     * @param B Continuous input matrix of the plant being controlled.
     * @param Q The state cost matrix.
     * @param R The input cost matrix.
     * @param N The state-input cross-term cost matrix.
     * @param dtSeconds Discretization timestep.
     */
    constructor(
        A: Matrix<States, States>?,
        B: Matrix<States, Inputs>,
        Q: Matrix<States, States>,
        R: Matrix<Inputs, Inputs>,
        N: Matrix<States, Inputs>,
        dtSeconds: Double
    ) {
        val (discA, discB) = discretizeAB<Num, Num>(A, B, dtSeconds)
        val S: Unit = Drake.discreteAlgebraicRiccatiEquation(discA, discB, Q, R, N)

        // K = (BᵀSB + R)⁻¹(BᵀSA + Nᵀ)
        m_K = discB
            .transpose()
            .times(S)
            .times(discB)
            .plus(R)
            .solve(discB.transpose().times(S).times(discA).plus(N.transpose()))
        m_r = Matrix(SimpleMatrix(B.getNumRows(), 1))
        m_u = Matrix(SimpleMatrix(B.getNumCols(), 1))
        reset()
    }

    /**
     * Constructs a controller with the given coefficients and plant.
     *
     * @param states The number of states.
     * @param inputs The number of inputs.
     * @param k The gain matrix.
     */
    constructor(
        states: Nat<States>?, inputs: Nat<Inputs>?, k: Matrix<Inputs, States>?
    ) {
        m_K = k
        m_r = Matrix(states, Nat.N1())
        m_u = Matrix(inputs, Nat.N1())
        reset()
    }

    /**
     * Returns the control input vector u.
     *
     * @return The control input.
     */
    val u: Matrix<Inputs, N1>?
        get() = m_u

    /**
     * Returns an element of the control input vector u.
     *
     * @param row Row of u.
     * @return The row of the control input vector.
     */
    fun getU(row: Int): Double {
        return m_u.get(row, 0)
    }

    /**
     * Returns the reference vector r.
     *
     * @return The reference vector.
     */
    val r: Matrix<States, N1>?
        get() = m_r

    /**
     * Returns an element of the reference vector r.
     *
     * @param row Row of r.
     * @return The row of the reference vector.
     */
    fun getR(row: Int): Double {
        return m_r.get(row, 0)
    }

    /**
     * Returns the controller matrix K.
     *
     * @return the controller matrix K.
     */
    val k: Matrix<Inputs, States>?
        get() = m_K

    /** Resets the controller.  */
    fun reset() {
        m_r.fill(0.0)
        m_u.fill(0.0)
    }

    /**
     * Returns the next output of the controller.
     *
     * @param x The current state x.
     * @return The next controller output.
     */
    fun calculate(x: Matrix<States, N1?>?): Matrix<Inputs, N1>? {
        m_u = m_K.times(m_r.minus(x))
        return m_u
    }

    /**
     * Returns the next output of the controller.
     *
     * @param x The current state x.
     * @param nextR the next reference vector r.
     * @return The next controller output.
     */
    fun calculate(x: Matrix<States, N1?>?, nextR: Matrix<States, N1>?): Matrix<Inputs, N1> {
        m_r = nextR
        return calculate(x)
    }

    /**
     * Adjusts LQR controller gain to compensate for a pure time delay in the input.
     *
     *
     * Linear-Quadratic regulator controller gains tend to be aggressive. If sensor measurements
     * are time-delayed too long, the LQR may be unstable. However, if we know the amount of delay, we
     * can compute the control based on where the system will be after the time delay.
     *
     *
     * See https://file.tavsys.net/control/controls-engineering-in-frc.pdf appendix C.4 for a
     * derivation.
     *
     * @param plant The plant being controlled.
     * @param dtSeconds Discretization timestep in seconds.
     * @param inputDelaySeconds Input time delay in seconds.
     */
//    fun latencyCompensate(
//        plant: LinearSystem<States, Inputs, Outputs>, dtSeconds: Double, inputDelaySeconds: Double
//    ) {
//        val (discA, discB) = discretizeAB<States, Inputs>(plant.getA(), plant.getB(), dtSeconds)
//        m_K = m_K.times(discA.minus(discB.times<Num>(m_K)).pow(inputDelaySeconds / dtSeconds))
//    }
}