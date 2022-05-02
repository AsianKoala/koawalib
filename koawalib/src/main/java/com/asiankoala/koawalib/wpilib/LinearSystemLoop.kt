// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package edu.wpi.first.math.system

import com.asiankoala.koawalib.wpilib.Num
import com.asiankoala.koawalib.wpilib.Numbers.N1
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.StateSpaceUtil
import edu.wpi.first.math.controller.LinearPlantInversionFeedforward
import edu.wpi.first.math.controller.LinearQuadraticRegulator
import org.apache.commons.math3.filter.KalmanFilter
import org.ejml.MatrixDimensionException
import org.ejml.simple.SimpleMatrix
import java.lang.String
import java.util.function.Function

/**
 * Combines a controller, feedforward, and observer for controlling a mechanism with full state
 * feedback.
 *
 *
 * For everything in this file, "inputs" and "outputs" are defined from the perspective of the
 * plant. This means U is an input and Y is an output (because you give the plant U (powers) and it
 * gives you back a Y (sensor values). This is the opposite of what they mean from the perspective
 * of the controller (U is an output because that's what goes to the motors and Y is an input
 * because that's what comes back from the sensors).
 *
 *
 * For more on the underlying math, read
 * https://file.tavsys.net/control/controls-engineering-in-frc.pdf.
 */
class LinearSystemLoop<States : Num?, Inputs : Num?, Outputs : Num?>(
    controller: LinearQuadraticRegulator<States, Inputs, Outputs>,
    feedforward: LinearPlantInversionFeedforward<States, Inputs, Outputs>,
    observer: KalmanFilter<States, Inputs, Outputs>,
    clampFunction: Function<Matrix<Inputs, N1?>?, Matrix<Inputs, N1?>?>
) {
    private val m_controller: LinearQuadraticRegulator<States, Inputs, Outputs>
    private val m_feedforward: LinearPlantInversionFeedforward<States, Inputs, Outputs>
    private val m_observer: KalmanFilter<States, Inputs, Outputs>
    private var m_nextR: Matrix<States, N1>? = null
    private var m_clampFunction: Function<Matrix<Inputs, N1>, Matrix<Inputs, N1>>? = null

    /**
     * Constructs a state-space loop with the given plant, controller, and observer. By default, the
     * initial reference is all zeros. Users should call reset with the initial system state before
     * enabling the loop. This constructor assumes that the input(s) to this system are voltage.
     *
     * @param plant State-space plant.
     * @param controller State-space controller.
     * @param observer State-space observer.
     * @param maxVoltageVolts The maximum voltage that can be applied. Commonly 12.
     * @param dtSeconds The nominal timestep.
     */
    constructor(
        plant: LinearSystem<States, Inputs, Outputs>?,
        controller: LinearQuadraticRegulator<States, Inputs, Outputs>?,
        observer: KalmanFilter<States, Inputs, Outputs>?,
        maxVoltageVolts: Double,
        dtSeconds: Double
    ) : this(
        controller,
        LinearPlantInversionFeedforward(plant, dtSeconds),
        observer,
        Function<Matrix<Inputs, N1?>, Matrix<Inputs, N1>> { u: Matrix<Inputs, N1?>? ->
            StateSpaceUtil.desaturateInputVector(
                u,
                maxVoltageVolts
            )
        }) {
    }

    /**
     * Constructs a state-space loop with the given plant, controller, and observer. By default, the
     * initial reference is all zeros. Users should call reset with the initial system state before
     * enabling the loop.
     *
     * @param plant State-space plant.
     * @param controller State-space controller.
     * @param observer State-space observer.
     * @param clampFunction The function used to clamp the input U.
     * @param dtSeconds The nominal timestep.
     */
    constructor(
        plant: LinearSystem<States, Inputs, Outputs>?,
        controller: LinearQuadraticRegulator<States, Inputs, Outputs>,
        observer: KalmanFilter<States, Inputs, Outputs>,
        clampFunction: Function<Matrix<Inputs, N1?>?, Matrix<Inputs, N1?>?>,
        dtSeconds: Double
    ) : this(
        controller,
        LinearPlantInversionFeedforward(plant, dtSeconds),
        observer,
        clampFunction
    ) {
    }

    /**
     * Constructs a state-space loop with the given controller, feedforward and observer. By default,
     * the initial reference is all zeros. Users should call reset with the initial system state
     * before enabling the loop.
     *
     * @param controller State-space controller.
     * @param feedforward Plant inversion feedforward.
     * @param observer State-space observer.
     * @param maxVoltageVolts The maximum voltage that can be applied. Assumes that the inputs are
     * voltages.
     */
    constructor(
        controller: LinearQuadraticRegulator<States, Inputs, Outputs>?,
        feedforward: LinearPlantInversionFeedforward<States, Inputs, Outputs>?,
        observer: KalmanFilter<States, Inputs, Outputs>?,
        maxVoltageVolts: Double
    ) : this(
        controller,
        feedforward,
        observer,
        Function<Matrix<Inputs, N1?>, Matrix<Inputs, N1>> { u: Matrix<Inputs, N1?>? ->
            StateSpaceUtil.desaturateInputVector(
                u,
                maxVoltageVolts
            )
        }) {
    }
    /**
     * Returns the observer's state estimate x-hat.
     *
     * @return the observer's state estimate x-hat.
     */
    /**
     * Set the initial state estimate x-hat.
     *
     * @param xhat The initial state estimate x-hat.
     */
    var xHat: Matrix<States, N1>
        get() = observer.getXhat()
        set(xhat) {
            observer.setXhat(xhat)
        }

    /**
     * Returns an element of the observer's state estimate x-hat.
     *
     * @param row Row of x-hat.
     * @return the i-th element of the observer's state estimate x-hat.
     */
    fun getXHat(row: Int): Double {
        return observer.getXhat(row)
    }

    /**
     * Set an element of the initial state estimate x-hat.
     *
     * @param row Row of x-hat.
     * @param value Value for element of x-hat.
     */
    fun setXHat(row: Int, value: Double) {
        observer.setXhat(row, value)
    }

    /**
     * Returns an element of the controller's next reference r.
     *
     * @param row Row of r.
     * @return the element i of the controller's next reference r.
     */
    fun getNextR(row: Int): Double {
        return nextR.get(row, 0)
    }
    /**
     * Returns the controller's next reference r.
     *
     * @return the controller's next reference r.
     */
    /**
     * Set the next reference r.
     *
     * @param nextR Next reference.
     */
    var nextR: Matrix<States, N1>?
        get() = m_nextR
        set(nextR) {
            m_nextR = nextR
        }

    /**
     * Set the next reference r.
     *
     * @param nextR Next reference.
     */
    fun setNextR(vararg nextR: Double) {
        if (nextR.size != m_nextR.getNumRows()) {
            throw MatrixDimensionException(
                String.format(
                    "The next reference does not have the "
                            + "correct number of entries! Expected %s, but got %s.",
                    m_nextR.getNumRows(), nextR.size
                )
            )
        }
        m_nextR = Matrix(SimpleMatrix(m_nextR.getNumRows(), 1, true, nextR))
    }

    /**
     * Returns the controller's calculated control input u plus the calculated feedforward u_ff.
     *
     * @return the calculated control input u.
     */
    val u: Matrix<Inputs, N1>
        get() = clampInput(m_controller.getU().plus(m_feedforward.getUff()))

    /**
     * Returns an element of the controller's calculated control input u.
     *
     * @param row Row of u.
     * @return the calculated control input u at the row i.
     */
    fun getU(row: Int): Double {
        return u.get(row, 0)
    }

    /**
     * Return the controller used internally.
     *
     * @return the controller used internally.
     */
    val controller: LinearQuadraticRegulator<States, Inputs, Outputs>
        get() = m_controller

    /**
     * Return the feedforward used internally.
     *
     * @return the feedforward used internally.
     */
    val feedforward: LinearPlantInversionFeedforward<States, Inputs, Outputs>
        get() = m_feedforward

    /**
     * Return the observer used internally.
     *
     * @return the observer used internally.
     */
    val observer: KalmanFilter<States, Inputs, Outputs>
        get() = m_observer

    /**
     * Zeroes reference r and controller output u. The previous reference of the
     * PlantInversionFeedforward and the initial state estimate of the KalmanFilter are set to the
     * initial state provided.
     *
     * @param initialState The initial state.
     */
    fun reset(initialState: Matrix<States, N1>?) {
        m_nextR.fill(0.0)
        m_controller.reset()
        m_feedforward.reset(initialState)
        m_observer.setXhat(initialState)
    }

    /**
     * Returns difference between reference r and current state x-hat.
     *
     * @return The state error matrix.
     */
    val error: Matrix<States, N1>
        get() = controller.getR().minus(m_observer.getXhat())

    /**
     * Returns difference between reference r and current state x-hat.
     *
     * @param index The index of the error matrix to return.
     * @return The error at that index.
     */
    fun getError(index: Int): Double {
        return (controller.getR().minus(m_observer.getXhat())).get(index, 0)
    }

    /**
     * Get the function used to clamp the input u.
     *
     * @return The clamping function.
     */
    val clampFunction: Function<Any, Any>?
        get() = m_clampFunction

    /**
     * Set the clamping function used to clamp inputs.
     *
     * @param clampFunction The clamping function.
     */
    fun setClampFunction(clampFunction: Function<Matrix<Inputs, N1?>?, Matrix<Inputs, N1?>?>) {
        m_clampFunction = clampFunction
    }

    /**
     * Correct the state estimate x-hat using the measurements in y.
     *
     * @param y Measurement vector.
     */
    fun correct(y: Matrix<Outputs, N1?>?) {
        observer.correct(u, y)
    }

    /**
     * Sets new controller output, projects model forward, and runs observer prediction.
     *
     *
     * After calling this, the user should send the elements of u to the actuators.
     *
     * @param dtSeconds Timestep for model update.
     */
    fun predict(dtSeconds: Double) {
        val u: Matrix<Inputs, N1> = clampInput(
            m_controller
                .calculate(observer.getXhat(), m_nextR)
                .plus(m_feedforward.calculate(m_nextR))
        )
        observer.predict(u, dtSeconds)
    }

    /**
     * Clamp the input u to the min and max.
     *
     * @param unclampedU The input to clamp.
     * @return The clamped input.
     */
    fun clampInput(unclampedU: Matrix<Inputs, N1?>?): Matrix<Inputs, N1> {
        return m_clampFunction.apply(unclampedU)
    }

    /**
     * Constructs a state-space loop with the given controller, feedforward, and observer. By default,
     * the initial reference is all zeros. Users should call reset with the initial system state
     * before enabling the loop.
     *
     * @param controller State-space controller.
     * @param feedforward Plant inversion feedforward.
     * @param observer State-space observer.
     * @param clampFunction The function used to clamp the input U.
     */
    init {
        m_controller = controller
        m_feedforward = feedforward
        m_observer = observer
        m_clampFunction = clampFunction
        m_nextR = Matrix(SimpleMatrix(controller.getK().getNumCols(), 1))
        reset(m_nextR)
    }
}