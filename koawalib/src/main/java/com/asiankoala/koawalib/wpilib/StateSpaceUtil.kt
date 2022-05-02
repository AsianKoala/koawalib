// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package edu.wpi.first.math

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.asiankoala.koawalib.wpilib.Nat
import com.asiankoala.koawalib.wpilib.Num
import com.asiankoala.koawalib.wpilib.Numbers.N1
import com.asiankoala.koawalib.wpilib.Numbers.N3
import com.asiankoala.koawalib.wpilib.Numbers.N4
import org.ejml.simple.SimpleMatrix
import java.util.*

object StateSpaceUtil {
    private val rand = Random()

    /**
     * Creates a covariance matrix from the given vector for use with Kalman filters.
     *
     *
     * Each element is squared and placed on the covariance matrix diagonal.
     *
     * @param <States> Num representing the states of the system.
     * @param states A Nat representing the states of the system.
     * @param stdDevs For a Q matrix, its elements are the standard deviations of each state from how
     * the model behaves. For an R matrix, its elements are the standard deviations for each
     * output measurement.
     * @return Process noise or measurement noise covariance matrix.
    </States> */
    fun <States : Num?> makeCovarianceMatrix(
        states: Nat<States>, stdDevs: Matrix<States, N1?>
    ): Matrix<States, States> {
        val result = Matrix(states, states)
        for (i in 0 until states.getNum()) {
            result.set(i, i, Math.pow(stdDevs.get(i, 0), 2.0))
        }
        return result
    }

    /**
     * Creates a vector of normally distributed white noise with the given noise intensities for each
     * element.
     *
     * @param <N> Num representing the dimensionality of the noise vector to create.
     * @param stdDevs A matrix whose elements are the standard deviations of each element of the noise
     * vector.
     * @return White noise vector.
    </N> */
    fun <N : Num?> makeWhiteNoiseVector(stdDevs: Matrix<N, N1?>): Matrix<N, N1> {
        val result: Matrix<N, N1> = Matrix(SimpleMatrix(stdDevs.getNumRows(), 1))
        for (i in 0 until stdDevs.getNumRows()) {
            result.set(i, 0, rand.nextGaussian() * stdDevs.get(i, 0))
        }
        return result
    }

    /**
     * Creates a cost matrix from the given vector for use with LQR.
     *
     *
     * The cost matrix is constructed using Bryson's rule. The inverse square of each tolerance is
     * placed on the cost matrix diagonal. If a tolerance is infinity, its cost matrix entry is set to
     * zero.
     *
     * @param <Elements> Nat representing the number of system states or inputs.
     * @param tolerances An array. For a Q matrix, its elements are the maximum allowed excursions of
     * the states from the reference. For an R matrix, its elements are the maximum allowed
     * excursions of the control inputs from no actuation.
     * @return State excursion or control effort cost matrix.
    </Elements> */
    fun <Elements : Num?> makeCostMatrix(
        tolerances: Matrix<Elements, N1?>
    ): Matrix<Elements, Elements> {
        val result: Matrix<Elements, Elements> =
            Matrix(SimpleMatrix(tolerances.getNumRows(), tolerances.getNumRows()))
        result.fill(0.0)
        for (i in 0 until tolerances.getNumRows()) {
            if (tolerances.get(i, 0) === Double.POSITIVE_INFINITY) {
                result.set(i, i, 0.0)
            } else {
                result.set(i, i, 1.0 / Math.pow(tolerances.get(i, 0), 2.0))
            }
        }
        return result
    }

    /**
     * Returns true if (A, B) is a stabilizable pair.
     *
     *
     * (A, B) is stabilizable if and only if the uncontrollable eigenvalues of A, if any, have
     * absolute values less than one, where an eigenvalue is uncontrollable if rank(λI - A, B) %3C n
     * where n is the number of states.
     *
     * @param <States> Num representing the size of A.
     * @param <Inputs> Num representing the columns of B.
     * @param A System matrix.
     * @param B Input matrix.
     * @return If the system is stabilizable.
    </Inputs></States> */
    fun <States : Num?, Inputs : Num?> isStabilizable(
        A: Matrix<States, States>, B: Matrix<States, Inputs>
    ): Boolean {
        return WPIMathJNI.isStabilizable(A.getNumRows(), B.getNumCols(), A.getData(), B.getData())
    }

    /**
     * Returns true if (A, C) is a detectable pair.
     *
     *
     * (A, C) is detectable if and only if the unobservable eigenvalues of A, if any, have absolute
     * values less than one, where an eigenvalue is unobservable if rank(λI - A; C) %3C n where n is
     * the number of states.
     *
     * @param <States> Num representing the size of A.
     * @param <Outputs> Num representing the rows of C.
     * @param A System matrix.
     * @param C Output matrix.
     * @return If the system is detectable.
    </Outputs></States> */
    fun <States : Num?, Outputs : Num?> isDetectable(
        A: Matrix<States, States>, C: Matrix<Outputs, States>
    ): Boolean {
        return WPIMathJNI.isStabilizable(
            A.getNumRows(), C.getNumRows(), A.transpose().getData(), C.transpose().getData()
        )
    }

    /**
     * Convert a [Pose2d] to a vector of [x, y, theta], where theta is in radians.
     *
     * @param pose A pose to convert to a vector.
     * @return The given pose in vector form, with the third element, theta, in radians.
     */
    fun poseToVector(pose: Pose2d): Matrix<N3, N1> {
        return VecBuilder.fill(pose.x, pose.y, pose.getRotation().getRadians())
    }

    /**
     * Clamp the input u to the min and max.
     *
     * @param u The input to clamp.
     * @param umin The minimum input magnitude.
     * @param umax The maximum input magnitude.
     * @param <I> The number of inputs.
     * @return The clamped input.
    </I> */
    fun <I : Num?> clampInputMaxMagnitude(
        u: Matrix<I, N1?>, umin: Matrix<I, N1?>, umax: Matrix<I, N1?>
    ): Matrix<I, N1> {
        val result: Matrix<I, N1> = Matrix<I, N1>(SimpleMatrix(u.getNumRows(), 1))
        for (i in 0 until u.getNumRows()) {
            result.set(i, 0, MathUtil.clamp(u.get(i, 0), umin.get(i, 0), umax.get(i, 0)))
        }
        return result
    }

    /**
     * Renormalize all inputs if any exceeds the maximum magnitude. Useful for systems such as
     * differential drivetrains.
     *
     * @param u The input vector.
     * @param maxMagnitude The maximum magnitude any input can have.
     * @param <I> The number of inputs.
     * @return The normalizedInput
    </I> */
    fun <I : Num?> desaturateInputVector(
        u: Matrix<I, N1>, maxMagnitude: Double
    ): Matrix<I, N1> {
        val maxValue: Double = u.maxAbs()
        val isCapped = maxValue > maxMagnitude
        return if (isCapped) {
            u.times(maxMagnitude / maxValue)
        } else u
    }

    /**
     * Convert a [Pose2d] to a vector of [x, y, cos(theta), sin(theta)], where theta is in
     * radians.
     *
     * @param pose A pose to convert to a vector.
     * @return The given pose in as a 4x1 vector of x, y, cos(theta), and sin(theta).
     */
    fun poseTo4dVector(pose: Pose2d): Matrix<N4, N1> {
        return VecBuilder.fill(
            pose.getTranslation().getX(),
            pose.getTranslation().getY(),
            pose.getRotation().getCos(),
            pose.getRotation().getSin()
        )
    }

    /**
     * Convert a [Pose2d] to a vector of [x, y, theta], where theta is in radians.
     *
     * @param pose A pose to convert to a vector.
     * @return The given pose in vector form, with the third element, theta, in radians.
     */
    fun poseTo3dVector(pose: Pose2d): Matrix<N3, N1> {
        return VecBuilder.fill(
            pose.getTranslation().getX(),
            pose.getTranslation().getY(),
            pose.getRotation().getRadians()
        )
    }
}