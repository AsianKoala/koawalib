// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package com.asiankoala.koawalib.wpilib.system

import com.asiankoala.koawalib.wpilib.Matrix
import com.asiankoala.koawalib.wpilib.Num
import org.ejml.simple.SimpleMatrix

object Discretization {
    /**
     * Discretizes the given continuous A matrix.
     *
     * @param <States> Num representing the number of states.
     * @param contA Continuous system matrix.
     * @param dtSeconds Discretization timestep.
     * @return the discrete matrix system.
    </States> */
    fun <States : Num> discretizeA(
        contA: Matrix<States, States>, dtSeconds: Double
    ): Matrix<States, States> {
        return contA.times(dtSeconds).exp()
    }

    /**
     * Discretizes the given continuous A and B matrices.
     *
     * @param <States> Nat representing the states of the system.
     * @param <Inputs> Nat representing the inputs to the system.
     * @param contA Continuous system matrix.
     * @param contB Continuous input matrix.
     * @param dtSeconds Discretization timestep.
     * @return a Pair representing discA and diskB.
    </Inputs></States> */
    fun <States : Num, Inputs : Num> discretizeAB(
        contA: Matrix<States, States>, contB: Matrix<States, Inputs>, dtSeconds: Double
    ): Pair<Matrix<States, States>, Matrix<States, Inputs>> {
        val scaledA = contA.times(dtSeconds)
        val scaledB = contB.times(dtSeconds)
        val states = contA.rows
        val inputs = contB.cols
        val M = Matrix(SimpleMatrix(states + inputs, states + inputs))
        M.assignBlock(0, 0, scaledA)
        M.assignBlock(0, scaledA.cols, scaledB)
        val phi: Unit = M.exp()
        val discA: Matrix<States, States> = Matrix<States, States>(SimpleMatrix(states, states))
        val discB: Matrix<States, Inputs> = Matrix<States, Inputs>(SimpleMatrix(states, inputs))
        discA.extractFrom(0, 0, phi)
        discB.extractFrom(0, contB.rows, phi)
        return Pair(discA, discB)
    }

    /**
     * Discretizes the given continuous A and Q matrices.
     *
     * @param <States> Nat representing the number of states.
     * @param contA Continuous system matrix.
     * @param contQ Continuous process noise covariance matrix.
     * @param dtSeconds Discretization timestep.
     * @return a pair representing the discrete system matrix and process noise covariance matrix.
    </States> */
    fun <States : Num> discretizeAQ(
        contA: Matrix<States, States>, contQ: Matrix<States, States>, dtSeconds: Double
    ): Pair<Matrix<States, States>, Matrix<States, States>> {
        val states: Int = contA.getNumRows()

        // Make continuous Q symmetric if it isn't already
        var Q: Matrix<States, States> = contQ.plus(contQ.transpose()).div(2.0)

        // Set up the matrix M = [[-A, Q], [0, A.T]]
        val M = Matrix(SimpleMatrix(2 * states, 2 * states))
        M.assignBlock(0, 0, contA.times(-1.0))
        M.assignBlock(0, states, Q)
        M.assignBlock(states, 0, Matrix(SimpleMatrix(states, states)))
        M.assignBlock(states, states, contA.transpose())
        val phi: Unit = M.times(dtSeconds).exp()

        // Phi12 = phi[0:States,        States:2*States]
        // Phi22 = phi[States:2*States, States:2*States]
        val phi12: Matrix<States, States> = phi.block(states, states, 0, states)
        val phi22: Matrix<States, States> = phi.block(states, states, states, states)
        val discA: Unit = phi22.transpose()
        Q = discA.times(phi12)

        // Make discrete Q symmetric if it isn't already
        val discQ: Unit = Q.plus(Q.transpose()).div(2.0)
        return Pair(discA, discQ)
    }

    /**
     * Discretizes the given continuous A and Q matrices.
     *
     *
     * Rather than solving a 2N x 2N matrix exponential like in DiscretizeQ() (which is expensive),
     * we take advantage of the structure of the block matrix of A and Q.
     *
     *
     * The exponential of A*t, which is only N x N, is relatively cheap. 2) The upper-right quarter
     * of the 2N x 2N matrix, which we can approximate using a taylor series to several terms and
     * still be substantially cheaper than taking the big exponential.
     *
     * @param <States> Nat representing the number of states.
     * @param contA Continuous system matrix.
     * @param contQ Continuous process noise covariance matrix.
     * @param dtSeconds Discretization timestep.
     * @return a pair representing the discrete system matrix and process noise covariance matrix.
    </States> */
    fun <States : Num> discretizeAQTaylor(
        contA: Matrix<States, States>, contQ: Matrix<States, States>, dtSeconds: Double
    ): Pair<Matrix<States, States>, Matrix<States, States>> {
        // Make continuous Q symmetric if it isn't already
        var Q: Matrix<States, States> = contQ.plus(contQ.transpose()).div(2.0)
        var lastTerm: Matrix<States, States> = Q.copy()
        var lastCoeff = dtSeconds

        // Aᵀⁿ
        var Atn: Matrix<States, States> = contA.transpose()
        var phi12: Matrix<States, States> = lastTerm.times(lastCoeff)

        // i = 6 i.e. 5th order should be enough precision
        for (i in 2..5) {
            lastTerm = contA.times(-1).times(lastTerm).plus(Q.times(Atn))
            lastCoeff *= dtSeconds / i.toDouble()
            phi12 = phi12.plus(lastTerm.times(lastCoeff))
            Atn = Atn.times(contA.transpose())
        }
        val discA: Matrix<States, States> = discretizeA<Num>(contA, dtSeconds)
        Q = discA.times(phi12)

        // Make Q symmetric if it isn't already
        val discQ: Unit = Q.plus(Q.transpose()).div(2.0)
        return Pair(discA, discQ)
    }

    /**
     * Returns a discretized version of the provided continuous measurement noise covariance matrix.
     * Note that dt=0.0 divides R by zero.
     *
     * @param <O> Nat representing the number of outputs.
     * @param R Continuous measurement noise covariance matrix.
     * @param dtSeconds Discretization timestep.
     * @return Discretized version of the provided continuous measurement noise covariance matrix.
    </O> */
    fun <O : Num> discretizeR(R: Matrix<O, O>, dtSeconds: Double): Matrix<O, O> {
        return R.div(dtSeconds)
    }
}