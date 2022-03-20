package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.command.CommandOpMode
import com.asiankoala.koawalib.command.CommandOpMode.Companion.logger
import com.asiankoala.koawalib.math.MathUtil.cos
import com.asiankoala.koawalib.math.MathUtil.degrees
import com.asiankoala.koawalib.math.MathUtil.sin
import com.asiankoala.koawalib.math.MathUtil.wrap
import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.TimePose
import com.asiankoala.koawalib.subsystem.DeviceSubsystem
import kotlin.math.absoluteValue
import kotlin.math.max

open class Odometry(@JvmField val config: OdoConfig) : DeviceSubsystem(), Localized {
    private var _position = Pose()
    override val position: Pose get() = _position

    override val velocity: Pose
        get() {
            if (prevRobotRelativePositions.size < 2) {
                return Pose()
            }

            val oldIndex = max(0, prevRobotRelativePositions.size - config.VELOCITY_READ_TICKS - 1)
            val old = prevRobotRelativePositions[oldIndex]
            val curr = prevRobotRelativePositions[prevRobotRelativePositions.size - 1]

            val scalar = (curr.timestamp - old.timestamp).toDouble() / 1000.0

            val dirVel = (curr.pose.point - old.pose.point) * (1 / scalar)
            val angularVel = (curr.pose.heading - old.pose.heading) * (1 / scalar)

            return Pose(dirVel, angularVel.wrap)
        }

    var startPose = Pose()
        set(value) {
            _position = value
            field = value
        }

    private var leftOffset = 0.0
    private var rightOffset = 0.0
    private var auxOffset = 0.0

    private var lastLeftEncoder = 0.0
    private var lastRightEncoder = 0.0
    private var lastAuxEncoder = 0.0

    private var currLeftEncoder = { config.leftEncoder.position }
    private var currRightEncoder = { config.rightEncoder.position }
    private var currAuxEncoder = { config.auxEncoder.position }

    private var accumulatedHeading = 0.0
    private var accumulatedRX = 0.0
    private var accumulatedAux = 0.0

    private val prevRobotRelativePositions = ArrayList<TimePose>()
    private var robotRelativeMovement = Pose()

    // TODO: MAKE KoawaDashboard V2
    fun updateTelemetry() {
        
        logger.addTelemetryData("left encoder", lastLeftEncoder)
        logger.addTelemetryData("right encoder", lastRightEncoder)
        logger.addTelemetryData("aux encoder", lastAuxEncoder)
        logger.addTelemetryData("left offset", leftOffset)
        logger.addTelemetryData("right offset", rightOffset)
        logger.addTelemetryData("aux offset", auxOffset)
        logger.addTelemetryData("accumulated heading", accumulatedHeading.degrees)
        logger.addTelemetryData("start pose", startPose.degString)
        logger.addTelemetryData("curr pose", position.degString)
        logger.addTelemetryData("corrected aux tracker", calculateAuxTracker())
    }

    // TODO: test if works
    fun calculateAuxTracker(): Double {
        /**
         * assuming no translational movement and N full rotations
         * ∫ aux - ∫ tracked = ∫ relativeX
         * tracked = dtheta * tracker
         * ∫ aux - ∫ dtheta * tracker = ∫ relativeX
         * ∫ aux - tracker ∫ dtheta = ∫ relativeX
         * ∫ aux - ∫ relativeX = tracker ∫ dtheta
         * (∫ aux - ∫ relativeX) / (∫ dtheta) - tracker_0 = tracker
         */
        return (accumulatedAux - accumulatedRX) / accumulatedHeading - config.AUX_TRACKER
    }

    override fun localize() {
        val currLeft = currLeftEncoder.invoke()
        val currRight = currRightEncoder.invoke()
        val currAux = currAuxEncoder.invoke()

        val actualCurrLeft = config.LEFT_SCALAR * (currLeft - leftOffset)
        val actualCurrRight = config.RIGHT_SCALAR * (currRight - rightOffset)
        val actualCurrAux = config.AUX_SCALAR * (currAux - auxOffset)

        val lWheelDelta = (actualCurrLeft - lastLeftEncoder) / config.TICKS_PER_INCH
        val rWheelDelta = (actualCurrRight - lastRightEncoder) / config.TICKS_PER_INCH
        val aWheelDelta = (actualCurrAux - lastAuxEncoder) / config.TICKS_PER_INCH

        val leftTotal = actualCurrLeft / config.TICKS_PER_INCH
        val rightTotal = actualCurrRight / config.TICKS_PER_INCH

        val lastAngle = _position.heading
        val newAngle = (((leftTotal - rightTotal) / config.TURN_SCALAR) + startPose.heading).wrap

        val angleIncrement = (lWheelDelta - rWheelDelta) / config.TURN_SCALAR
        val auxPrediction = angleIncrement * config.AUX_TRACKER
        val rX = aWheelDelta - auxPrediction

        accumulatedHeading += angleIncrement
        accumulatedRX += rX
        accumulatedAux += aWheelDelta

        var deltaY = (lWheelDelta - rWheelDelta) / 2.0
        var deltaX = rX

        if (angleIncrement.absoluteValue > 0) {
            val radiusOfMovement = (lWheelDelta + rWheelDelta) / (2 * angleIncrement)
            val radiusOfStrafe = rX / angleIncrement

            deltaX = (radiusOfMovement * (1 - angleIncrement.cos)) + (radiusOfStrafe * angleIncrement.sin)
            deltaY = (radiusOfMovement * angleIncrement.sin) + (radiusOfStrafe * (1 - angleIncrement.cos))
        }

        val robotDeltaRelativeMovement = Pose(deltaX, deltaY, angleIncrement)
        robotRelativeMovement += robotDeltaRelativeMovement

        prevRobotRelativePositions.add(TimePose(robotRelativeMovement))

        val incrementX = lastAngle.cos * deltaY - lastAngle.sin * deltaX
        val incrementY = lastAngle.sin * deltaY + lastAngle.cos * deltaX
        val pointIncrement = Point(incrementX, incrementY)

        _position = Pose(_position.point + pointIncrement, newAngle)

        lastLeftEncoder = actualCurrLeft
        lastRightEncoder = actualCurrRight
        lastAuxEncoder = actualCurrAux
    }
}
