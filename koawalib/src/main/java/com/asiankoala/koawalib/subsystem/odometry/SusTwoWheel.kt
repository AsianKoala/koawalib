package com.asiankoala.koawalib.subsystem.odometry

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.kinematics.Kinematics
import com.acmerobotics.roadrunner.util.Angle
import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.math.Pose
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.DecompositionSolver
import org.apache.commons.math3.linear.LUDecomposition
import org.apache.commons.math3.linear.MatrixUtils

class SusTwoWheel(config: OdoConfig, private val imu: KIMU, private val wheelPoses: List<Pose>) : Odometry(config) {
    private var leftEncoder = Encoder(config.leftEncoder, config.TICKS_PER_INCH)
    private var auxEncoder = Encoder(config.auxEncoder, config.TICKS_PER_INCH)
    private val forwardSolver: DecompositionSolver
    private var lastWheelPositions = emptyList<Double>()
    private var lastHeading = Double.NaN
    private var _poseEstimate = Pose()
    var poseEstimate: Pose
        get() = _poseEstimate
        set(value) {
            lastWheelPositions = emptyList()
            lastHeading = Double.NaN
            _poseEstimate = value
        }

    fun Pose2d.toPose(): Pose {
        return Pose(this.x, this.y, this.heading)
    }

    fun Pose.toPose2d(): Pose2d {
        return Pose2d(this.x, this.y, this.heading)
    }

    override fun localize() {
        leftEncoder.read()
        auxEncoder.read()
        val wheelPositions = listOf(leftEncoder.currRead, auxEncoder.currRead)
        imu.periodic()
        val heading = imu.heading
        if (lastWheelPositions.isNotEmpty()) {
            val wheelDeltas = wheelPositions
                .zip(lastWheelPositions)
                .map { it.first - it.second }
            val headingDelta = Angle.normDelta(heading - lastHeading)
            val robotPoseDelta = calculatePoseDelta(wheelDeltas, headingDelta)
            _poseEstimate = Kinematics.relativeOdometryUpdate(_poseEstimate.toPose2d(), robotPoseDelta.toPose2d()).toPose()
        }

        lastWheelPositions = wheelPositions
        lastHeading = heading
    }

    override fun updateTelemetry() {

    }

    private fun calculatePoseDelta(wheelDeltas: List<Double>, headingDelta: Double): Pose {
        val rawPoseDelta = forwardSolver.solve(
            MatrixUtils.createRealMatrix(
                arrayOf((wheelDeltas + headingDelta).toDoubleArray())
            ).transpose()
        )
        return Pose(
            rawPoseDelta.getEntry(0, 0),
            rawPoseDelta.getEntry(1, 0),
            rawPoseDelta.getEntry(2, 0)
        )
    }

    init {

        val inverseMatrix = Array2DRowRealMatrix(3, 3)
        for (i in 0..1) {
            val orientationVector = wheelPoses[i].directionVector()
            val positionVector = wheelPoses[i].point
            inverseMatrix.setEntry(i, 0, orientationVector.x)
            inverseMatrix.setEntry(i, 1, orientationVector.y)
            inverseMatrix.setEntry(
                i,
                2,
                positionVector.x * orientationVector.y - positionVector.y * orientationVector.x
            )
        }
        inverseMatrix.setEntry(2, 2, 1.0)

        forwardSolver = LUDecomposition(inverseMatrix).solver

        require(forwardSolver.isNonSingular) { "The specified configuration cannot support full localization" }
    }
}