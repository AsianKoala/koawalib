package com.asiankoala.koawalib.path.gvf

import com.acmerobotics.roadrunner.kinematics.Kinematics
import com.asiankoala.koawalib.control.controller.PIDFController
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.disp.*
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.epsilonEquals
import com.asiankoala.koawalib.path.HermitePath
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import kotlin.math.PI
import kotlin.math.sign

class HenoGVF(
    override val path: HermitePath,
    override val drive: KMecanumOdoDrive,
    kN: Double,
    private val kStatic: Double,
    private val kV: Double,
    private val kA: Double,
    private val constraints: DriveConstraints,
    headingCoeffs: PIDGains,
    errorMap: GuidingVectorField.ErrorMap = GuidingVectorField.ErrorMap.Linear()
) : GVFController {
    private val gvf = GuidingVectorField(path, kN, errorMap)
    private val profile = generateOnlineMotionProfile(
        DisplacementState(0.0),
        DisplacementState(0.0),
        path.length,
        generateConstraints(path, constraints)
    )
    private val headingController = PIDFController(headingCoeffs).apply { setInputBounds(-PI, PI) }
    private var lastVel: Double = 0.0
    private var lastError = Pose()
    override var isFinished = false
    override var disp: Double = 0.0

    override fun update() {
        disp = path.project(drive.pose.vec, disp)

        val gvfResult = gvf.getExtended(drive.pose.vec, disp)
        val pathPose = path[disp]
        val pathDeriv = path[disp, 1]
        val pathSecondDeriv = path[disp, 2]

        val fieldError = pathPose.vec - drive.pose.vec
        val error = calculatePoseError(pathPose, drive.pose)

        headingController.targetPosition = error.heading
        headingController.targetVelocity = pathDeriv.heading * lastVel * gvfResult.displacementDeriv

        val headingCorrection = headingController.update(0.0, drive.vel.heading)

        isFinished = isFinished || disp epsilonEquals path.length
        val gvfVector = if(isFinished) fieldError.unit else gvfResult.vector
        val gvfDeriv = if(isFinished) Vector() else gvfResult.deriv

        var targetVel: Vector
        var targetAccel: Vector
        var omega: Double
        var alpha: Double
        var profileState = DisplacementState(lastVel)

        var iters = 0
        do {
            targetVel = gvfVector * profileState.v
            targetAccel = gvfDeriv * profileState.v * profileState.v + gvfVector * profileState.a

            // Calculate derivs of displacement (along the path) with respect to time for calculating omega and alpha
            val displacementDeriv = gvfResult.displacementDeriv * profileState.v
            val pathOmega = pathDeriv.heading * displacementDeriv

            val denominator = 1.0 - ((drive.pose.vec - pathPose.vec) dot pathSecondDeriv.vec)
            val numerator1 =
                displacementDeriv * (targetVel dot pathSecondDeriv.vec) + (targetAccel dot pathDeriv.vec)
            val numerator2 = (targetVel dot pathDeriv.vec) * (targetVel dot pathSecondDeriv.vec)
            val displacementSecondDeriv =
                numerator1 / denominator + numerator2 / (denominator * denominator)
            val pathAlpha = pathSecondDeriv.heading * displacementDeriv * displacementDeriv
            +pathDeriv.heading * displacementSecondDeriv

            omega = (if (!isFinished) pathOmega else 0.0) + headingCorrection
            alpha = if (!isFinished) pathAlpha else 0.0

            val headingVelNormalized = if (omega epsilonEquals 0.0) 0.0 else omega / profileState.v
            val dynamicDeriv = Pose(gvfResult.vector, headingVelNormalized)

            val dynamicSecondDeriv = Pose(gvfResult.vector, pathSecondDeriv.heading)
            val constraints = constraints[disp, drive.pose, dynamicDeriv, dynamicSecondDeriv]

            val newProfileState = profile[disp, gvfResult.error, constraints]

            if(newProfileState.v epsilonEquals profileState.v) {
                profileState = newProfileState
                break
            }

            profileState = newProfileState
        } while (iters++ < 20)

        profile.update(profileState.v)

        val fieldVel = Pose(targetVel, omega)
        val fieldAccel = Pose(targetAccel, alpha)
        val robotVel = fieldToRobotVelocity(drive.pose, fieldVel)
        val robotAccel = fieldToRobotAcceleration(drive.pose, fieldVel, fieldAccel)

        lastError = error
        lastVel = profileState.v

        drive.powers = Pose(
            robotVel.vec.unit * kStatic + robotVel.vec * kV + robotAccel.vec * kA,
            robotVel.heading.sign * kStatic + robotVel.heading * kV + robotAccel.heading * kA
        )
    }
}