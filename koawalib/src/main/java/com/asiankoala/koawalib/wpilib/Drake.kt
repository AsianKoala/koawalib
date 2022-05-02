// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package edu.wpi.first.math

import com.asiankoala.koawalib.wpilib.Num
import org.ejml.simple.SimpleMatrix

object Drake {
    /**
     * Solves the discrete alegebraic Riccati equation.
     *
     * @param A System matrix.
     * @param B Input matrix.
     * @param Q State cost matrix.
     * @param R Input cost matrix.
     * @return Solution of DARE.
     */
    fun discreteAlgebraicRiccatiEquation(
        A: SimpleMatrix, B: SimpleMatrix, Q: SimpleMatrix, R: SimpleMatrix
    ): SimpleMatrix {
        val S = SimpleMatrix(A.numRows(), A.numCols())
        WPIMathJNI.discreteAlgebraicRiccatiEquation(
            A.ddrm.getData(),
            B.ddrm.getData(),
            Q.ddrm.getData(),
            R.ddrm.getData(),
            A.numCols(),
            B.numCols(),
            S.ddrm.getData()
        )
        return S
    }

    /**
     * Solves the discrete alegebraic Riccati equation.
     *
     * @param <States> Number of states.
     * @param <Inputs> Number of inputs.
     * @param A System matrix.
     * @param B Input matrix.
     * @param Q State cost matrix.
     * @param R Input cost matrix.
     * @return Solution of DARE.
    </Inputs></States> */
    fun <States : Num?, Inputs : Num?> discreteAlgebraicRiccatiEquation(
        A: Matrix<States, States>,
        B: Matrix<States, Inputs>,
        Q: Matrix<States, States>,
        R: Matrix<Inputs, Inputs>
    ): Matrix<States, States> {
        return Matrix(
            discreteAlgebraicRiccatiEquation(
                A.getStorage(), B.getStorage(), Q.getStorage(), R.getStorage()
            )
        )
    }

    /**
     * Solves the discrete alegebraic Riccati equation.
     *
     * @param A System matrix.
     * @param B Input matrix.
     * @param Q State cost matrix.
     * @param R Input cost matrix.
     * @param N State-input cross-term cost matrix.
     * @return Solution of DARE.
     */
    fun discreteAlgebraicRiccatiEquation(
        A: SimpleMatrix, B: SimpleMatrix, Q: SimpleMatrix, R: SimpleMatrix, N: SimpleMatrix
    ): SimpleMatrix {
        // See
        // https://en.wikipedia.org/wiki/Linear%E2%80%93quadratic_regulator#Infinite-horizon,_discrete-time_LQR
        // for the change of variables used here.
        val scrA = A.minus(B.mult(R.solve(N.transpose())))
        val scrQ = Q.minus(N.mult(R.solve(N.transpose())))
        val S = SimpleMatrix(A.numRows(), A.numCols())
        WPIMathJNI.discreteAlgebraicRiccatiEquation(
            scrA.ddrm.getData(),
            B.ddrm.getData(),
            scrQ.ddrm.getData(),
            R.ddrm.getData(),
            A.numCols(),
            B.numCols(),
            S.ddrm.getData()
        )
        return S
    }

    /**
     * Solves the discrete alegebraic Riccati equation.
     *
     * @param <States> Number of states.
     * @param <Inputs> Number of inputs.
     * @param A System matrix.
     * @param B Input matrix.
     * @param Q State cost matrix.
     * @param R Input cost matrix.
     * @param N State-input cross-term cost matrix.
     * @return Solution of DARE.
    </Inputs></States> */
    fun <States : Num?, Inputs : Num?> discreteAlgebraicRiccatiEquation(
        A: Matrix<States, States>,
        B: Matrix<States, Inputs>,
        Q: Matrix<States, States>,
        R: Matrix<Inputs, Inputs>,
        N: Matrix<States, Inputs>
    ): Matrix<States, States> {
        // See
        // https://en.wikipedia.org/wiki/Linear%E2%80%93quadratic_regulator#Infinite-horizon,_discrete-time_LQR
        // for the change of variables used here.
        val scrA: Unit = A.minus(B.times(R.solve(N.transpose())))
        val scrQ: Unit = Q.minus(N.times(R.solve(N.transpose())))
        return Matrix(
            discreteAlgebraicRiccatiEquation(
                scrA.getStorage(), B.getStorage(), scrQ.getStorage(), R.getStorage()
            )
        )
    }
}