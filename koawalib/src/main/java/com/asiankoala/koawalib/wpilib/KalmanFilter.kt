// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package edu.wpi.first.math.estimator

import com.asiankoala.koawalib.wpilib.Discretization.discretizeAQTaylor
import com.asiankoala.koawalib.wpilib.Discretization.discretizeR
import com.asiankoala.koawalib.wpilib.Nat
import com.asiankoala.koawalib.wpilib.Num
import com.asiankoala.koawalib.wpilib.Numbers.N1
import edu.wpi.first.math.Drake
import edu.wpi.first.math.MathSharedStore
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.StateSpaceUtil
import edu.wpi.first.math.system.LinearSystem

/**
 * A Kalman filter combines predictions from a model and measurements to give an estimate of the
 * true system state. This is useful because many states cannot be measured directly as a result of
 * sensor noise, or because the state is "hidden".
 *
 *
 * Kalman filters use a K gain matrix to determine whether to trust the model or measurements
 * more. Kalman filter theory uses statistics to compute an optimal K gain which minimizes the sum
 * of squares error in the state estimate. This K gain is used to correct the state estimate by some
 * amount of the difference between the actual measurements and the measurements predicted by the
 * model.
 *
 *
 * For more on the underlying math, read
 * https://file.tavsys.net/control/controls-engineering-in-frc.pdf chapter 9 "Stochastic control
 * theory".
 */
class KalmanFilter<States : Num?, Inputs : Num?, Outputs : Num?>(
    private val m_states: Nat<States>,
    outputs: Nat<Outputs>?,
    plant: LinearSystem<States, Inputs, Outputs>,
    stateStdDevs: Matrix<States, N1?>?,
    measurementStdDevs: Matrix<Outputs, N1?>?,
    dtSeconds: Double
) {
    private val m_plant: LinearSystem<States, Inputs, Outputs>

    /** The steady-state Kalman gain matrix.  */
    private val m_K: Matrix<States, Outputs>

    /** The state estimate.  */
    private var m_xHat: Matrix<States, N1>? = null
    fun reset() {
        m_xHat = Matrix(m_states, Nat.N1())
    }

    /**
     * Returns the steady-state Kalman gain matrix K.
     *
     * @return The steady-state Kalman gain matrix K.
     */
    val k: Matrix<States, Outputs>
        get() = m_K

    /**
     * Returns an element of the steady-state Kalman gain matrix K.
     *
     * @param row Row of K.
     * @param col Column of K.
     * @return the element (i, j) of the steady-state Kalman gain matrix K.
     */
    fun getK(row: Int, col: Int): Double {
        return m_K.get(row, col)
    }

    /**
     * Set an element of the initial state estimate x-hat.
     *
     * @param row Row of x-hat.
     * @param value Value for element of x-hat.
     */
    fun setXhat(row: Int, value: Double) {
        m_xHat.set(row, 0, value)
    }
    /**
     * Returns the state estimate x-hat.
     *
     * @return The state estimate x-hat.
     */
    /**
     * Set initial state estimate x-hat.
     *
     * @param xhat The state estimate x-hat.
     */
    var xhat: Matrix<States, N1>?
        get() = m_xHat
        set(xhat) {
            m_xHat = xhat
        }

    /**
     * Returns an element of the state estimate x-hat.
     *
     * @param row Row of x-hat.
     * @return the state estimate x-hat at i.
     */
    fun getXhat(row: Int): Double {
        return m_xHat.get(row, 0)
    }

    /**
     * Project the model into the future with a new control input u.
     *
     * @param u New control input from controller.
     * @param dtSeconds Timestep for prediction.
     */
    fun predict(u: Matrix<Inputs, N1?>?, dtSeconds: Double) {
        m_xHat = m_plant.calculateX(m_xHat, u, dtSeconds)
    }

    /**
     * Correct the state estimate x-hat using the measurements in y.
     *
     * @param u Same control input used in the last predict step.
     * @param y Measurement vector.
     */
    fun correct(u: Matrix<Inputs, N1?>?, y: Matrix<Outputs, N1?>) {
        val C: Unit = m_plant.getC()
        val D: Unit = m_plant.getD()
        // x̂ₖ₊₁⁺ = x̂ₖ₊₁⁻ + K(y − (Cx̂ₖ₊₁⁻ + Duₖ₊₁))
        m_xHat = m_xHat.plus(m_K.times(y.minus(C.times(m_xHat).plus(D.times(u)))))
    }

    /**
     * Constructs a state-space observer with the given plant.
     *
     * @param states A Nat representing the states of the system.
     * @param outputs A Nat representing the outputs of the system.
     * @param plant The plant used for the prediction step.
     * @param stateStdDevs Standard deviations of model states.
     * @param measurementStdDevs Standard deviations of measurements.
     * @param dtSeconds Nominal discretization timestep.
     */
    init {
        m_plant = plant
        val contQ: Unit = StateSpaceUtil.makeCovarianceMatrix(m_states, stateStdDevs)
        val contR: Unit = StateSpaceUtil.makeCovarianceMatrix(outputs, measurementStdDevs)
        val (discA, discQ) = discretizeAQTaylor<States>(plant.getA(), contQ, dtSeconds)
        val discR = discretizeR<Num>(contR, dtSeconds)
        val C: Unit = plant.getC()
        if (!StateSpaceUtil.isDetectable(discA, C)) {
            val builder =
                StringBuilder("The system passed to the Kalman filter is unobservable!\n\nA =\n")
            builder
                .append(discA.storage.toString())
                .append("\nC =\n")
                .append(C.getStorage().toString())
                .append('\n')
            val msg = builder.toString()
            MathSharedStore.reportError(msg, Thread.currentThread().stackTrace)
            throw IllegalArgumentException(msg)
        }
        val P = Matrix(
            Drake.discreteAlgebraicRiccatiEquation(discA.transpose(), C.transpose(), discQ, discR)
        )

        // S = CPCᵀ + R
        val S: Unit = C.times(P).times(C.transpose()).plus(discR)

        // We want to put K = PCᵀS⁻¹ into Ax = b form so we can solve it more
        // efficiently.
        //
        // K = PCᵀS⁻¹
        // KS = PCᵀ
        // (KS)ᵀ = (PCᵀ)ᵀ
        // SᵀKᵀ = CPᵀ
        //
        // The solution of Ax = b can be found via x = A.solve(b).
        //
        // Kᵀ = Sᵀ.solve(CPᵀ)
        // K = (Sᵀ.solve(CPᵀ))ᵀ
        m_K = Matrix(
            S.transpose().getStorage().solve(C.times(P.transpose()).getStorage()).transpose()
        )
        reset()
    }
}