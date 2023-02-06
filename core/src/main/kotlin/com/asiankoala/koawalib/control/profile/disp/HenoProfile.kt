package com.asiankoala.koawalib.control.profile.disp

import com.asiankoala.koawalib.math.epsilonEquals
import kotlin.math.*


import com.acmerobotics.roadrunner.util.DoubleProgression
import com.acmerobotics.roadrunner.util.NanoClock
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.path.HermitePath
import kotlin.math.max
import kotlin.math.min

/**
 * Motion profile motion constraints.
 */
abstract class MotionConstraints {

    /**
     * Returns the motion constraints [s] units along the profile.
     */
    abstract operator fun get(s: Double): SimpleMotionConstraints

    open operator fun get(s: DoubleProgression): List<SimpleMotionConstraints> = s.map(::get)
}


/**
 * Constant velocity and acceleration constraints.
 *
 * @param maxVel constant maximum velocity
 * @param maxAccel constant maximum acceleration
 */
data class SimpleMotionConstraints(
    @JvmField var maxVel: Double,
    @JvmField var maxAccel: Double
)

fun cbrt(x: Double) = sign(x) * abs(x).pow(1.0 / 3.0)

fun solveQuadratic(a: Double, b: Double, c: Double): DoubleArray {
    if (a epsilonEquals 0.0) {
        return doubleArrayOf(-c / b)
    }

    val disc = b * b - 4 * a * c
    return when {
        disc epsilonEquals 0.0 -> doubleArrayOf(-b / (2 * a))
        disc > 0.0 -> doubleArrayOf(
            (-b + sqrt(disc)) / (2 * a),
            (-b - sqrt(disc)) / (2 * a)
        )
        else -> doubleArrayOf()
    }
}


fun solveCubic(a: Double, b: Double, c: Double, d: Double): DoubleArray {
    if (a epsilonEquals 0.0) {
        return solveQuadratic(b, c, d)
    }

    val a2 = b / a
    val a1 = c / a
    val a0 = d / a

    val lambda = a2 / 3.0
    val Q = (3.0 * a1 - a2 * a2) / 9.0
    val R = (9.0 * a1 * a2 - 27.0 * a0 - 2.0 * a2 * a2 * a2) / 54.0
    val Q3 = Q * Q * Q
    val R2 = R * R
    val D = Q3 + R2

    return when {
        D < 0.0 -> { // 3 unique reals
            val theta = acos(R / sqrt(-Q3))
            val sqrtQ = sqrt(-Q)
            doubleArrayOf(
                2.0 * sqrtQ * cos(theta / 3.0) - lambda,
                2.0 * sqrtQ * cos((theta + 2.0 * PI) / 3.0) - lambda,
                2.0 * sqrtQ * cos((theta + 4.0 * PI) / 3.0) - lambda
            )
        }
        D > 0.0 -> { // 1 real
            val sqrtD = sqrt(D)
            val S = cbrt(R + sqrtD)
            val T = cbrt(R - sqrtD)
            doubleArrayOf(S + T - lambda)
        }
        else -> { // 2 unique (3 total) reals
            val cbrtR = cbrt(R)
            doubleArrayOf(2.0 * cbrtR - lambda, -cbrtR - lambda)
        }
    }
}
/**
 * Kinematic state of a displacement profile at any given displacement.
 */
class DisplacementState @JvmOverloads constructor(val v: Double, val a: Double = 0.0, val j: Double = 0.0) {

    /**
     * Returns the [DisplacementState] at displacement [dx].
     *
     * @param dx the change in position to query
     */
    operator fun get(dx: Double): DisplacementState {
        val t = getTime(dx)
        return DisplacementState(
            v + a * t + j / 2 * t * t,
            a + j * t,
            j
        )
    }

    /**
     * Returns the time after [dx] based on the initial parameters
     *
     * @param dx the change in position to query
     */
    fun getTime(dx: Double): Double {
        // solving the cubic 1/6jt^3 + 1/2at^2 + vt + x0 = x
        val roots = solveCubic(j / 6.0, a / 2.0, v, -dx)
        // We want the nearest root with correct the sign (assuming an increasing displacement and time)
        return roots.filter { sign(it) == sign(dx) }.sorted().getOrElse(0) { roots[0] }
    }

    override fun toString() = String.format("(v=%.3f, a=%.3f, j=%.3f)", v, a, j)
}

/**
 * Segment of a displacement profile.
 *
 * @param start start displacement state
 * @param dx displacement delta
 */
class DisplacementSegment(val start: DisplacementState, val dx: Double) {

    /**
     * Returns the [DisplacementState] at displacement [dx].
     */
    operator fun get(x: Double) = start[x]

    /**
     * Returns the [DisplacementState] at the end of the segment (displacement [dx]).
     */
    fun end() = start[dx]

    /**
     * Returns the duration of the segment. This is based on the [start] state.
     */
    fun duration() = start.getTime(dx)

    /**
     * Returns a reversed version of the segment. Note: it isn't possible to reverse a segment completely so this
     * method only guarantees that the start and end velocities will be swapped.
     */
    fun reversed(): DisplacementSegment {
        val end = end()
        val state = DisplacementState(end.v, -end.a, end.j)
        return DisplacementSegment(state, dx)
    }

    override fun toString() = "($start, $dx)"
}



/**
 * Motion profile parameterized by displacement composed of displacement segments.
 * Note: Many of the methods assume that displacement is monotonically increasing and starts at zero. Consequently,
 * reversed, overshoot, and undershoot profiles are not directly supported.
 *
 * @param segments profile displacement segments
 */
class DisplacementProfile(segments: List<DisplacementSegment>) {
    internal val segments: MutableList<DisplacementSegment> = segments.toMutableList()

    /**
     * Returns the [DisplacementState] at displacement [x] (starting at zero.)
     */
    operator fun get(x: Double): DisplacementState {
        var remainingDisplacement = max(0.0, min(x, length()))
        for (segment in segments) {
            if (remainingDisplacement <= segment.dx) {
                return segment[remainingDisplacement]
            }
            remainingDisplacement -= segment.dx
        }
        return segments.lastOrNull()?.end() ?: DisplacementState(0.0)
    }

    /**
     * Returns the length of the displacement profile.
     */
    fun length() = segments.sumByDouble { it.dx }

    /**
     * Returns the duration of the displacement profile.
     */
    fun duration() = segments.sumByDouble { it.duration() }

    /**
     * Returns a reversed version of the displacement profile.
     */
    fun reversed() = DisplacementProfile(segments.map { it.reversed() }.reversed())

    /**
     * Returns the start [DisplacementState].
     */
    fun start() = get(0.0)

    /**
     * Returns the end [DisplacementState].
     */
    fun end() = get(length())
}
class OnlineMotionProfile(
    start: DisplacementState,
    private val goal: DisplacementState,
    private val length: Double,
    private val baseConstraints: MotionConstraints,
    private val clock: NanoClock = NanoClock.system(),
    private val backwardsPassProfile: DisplacementProfile? = null
) {
    private var lastVelocity: Double = start.v
    private var lastUpdateTimestamp: Double = clock.seconds()

    /**
     * Gets the velocity and numeric acceleration state based on the online mp forward pass and the
     * [DisplacementProfile] reverse pass.
     * Note: Calling [update] is required to update the internal state after a velocity has been selected.
     * @param displacement The displacement along the profile from `[0, length]`
     * @param error An optional error parameter that is factored into the stopping distance calculation for
     * multi-dimensional systems
     * @param constraints An optional parameter to override the constraints which can be useful for dynamic
     * situations
     */
    @JvmOverloads
    operator fun get(
        displacement: Double,
        error: Double = 0.0,
        constraints: SimpleMotionConstraints = baseConstraints[displacement]
    ): DisplacementState {
        val timestamp = clock.seconds()
        val dt = timestamp - lastUpdateTimestamp
        val remainingDisplacement = max(abs(length - displacement), abs(error))
        val maxVelToStop = sqrt(2.0 * constraints.maxAccel * remainingDisplacement) + goal.v
        val maxVelFromLast = lastVelocity + constraints.maxAccel * dt
        val maxVelForward = backwardsPassProfile?.get(displacement)?.v
        val maxOnlineVel = minOf(maxVelFromLast, maxVelToStop, constraints.maxVel)
        val velocity = maxVelForward?.let { min(it, maxOnlineVel) } ?: maxOnlineVel
        val acceleration = (velocity - lastVelocity) / dt

        return DisplacementState(velocity, acceleration)
    }

    /**
     * Updates the internal state (timestamp and previous velocity) after a given velocity has been selected
     * @param velocity The velocity set/used
     * @param timestamp An optional parameter to override the current timestamp which represents the time this velocity
     * was set
     */
    fun update(velocity: Double, timestamp: Double = clock.seconds()) {
        lastVelocity = velocity
        lastUpdateTimestamp = timestamp
    }
}

fun generateDisplacementProfile(
    start: DisplacementState,
    goal: DisplacementState,
    length: Double,
    constraints: MotionConstraints,
    resolution: Double = 0.25
): DisplacementProfile {
    // ds is an adjusted resolution that fits nicely within length
    val samples = ceil(length / resolution).toInt()
    val s = DoubleProgression.fromClosedInterval(0.0, length, samples)

    val constraintsList = constraints[s]

    // compute the forward states
    val forwardProfile = forwardPass(
        start,
        s,
        constraintsList
    )

    // compute the backward states
    val backwardProfile = forwardPass(
        goal,
        s,
        constraintsList.reversed()
    ).reversed()

    // merge the forward and backward states
    return mergeProfiles(forwardProfile, backwardProfile)
}

// See figure 3.4 of [Sprunk2008.pdf](http://www2.informatik.uni-freiburg.de/~lau/students/Sprunk2008.pdf)
private fun mergeProfiles(
    forwardProfile: DisplacementProfile,
    backwardProfile: DisplacementProfile
): DisplacementProfile {
    val forwardSegments = forwardProfile.segments.toMutableList()
    val backwardSegments = backwardProfile.segments.toMutableList()
    val finalSegments = mutableListOf<DisplacementSegment>()

    var i = 0
    while (i < forwardSegments.size && i < backwardSegments.size) {
        // retrieve the start states and displacement deltas
        var forwardSegment = forwardSegments[i]
        var backwardSegment = backwardSegments[i]

        // if there's a discrepancy in the displacements, split the the longer chunk in two and add the second
        // to the corresponding list; this guarantees that segments are always aligned
        if (!(forwardSegment.dx epsilonEquals backwardSegment.dx)) {
            if (forwardSegment.dx > backwardSegment.dx) {
                // forward longer
                forwardSegments.add(
                    i + 1,
                    DisplacementSegment(
                        forwardSegment[backwardSegment.dx], forwardSegment.dx - backwardSegment.dx
                    )
                )
                forwardSegment = DisplacementSegment(forwardSegment.start, backwardSegment.dx)
            } else {
                // backward longer
                backwardSegments.add(
                    i + 1,
                    DisplacementSegment(
                        backwardSegment[forwardSegment.dx], backwardSegment.dx - forwardSegment.dx
                    )
                )
                backwardSegment = DisplacementSegment(backwardSegment.start, forwardSegment.dx)
            }
        }

        // compute the end states (after alignment)
        val (higherSegment, lowerSegment) =
            if (forwardSegment.start.v <= backwardSegment.start.v) {
                Pair(backwardSegment, forwardSegment)
            } else {
                Pair(forwardSegment, backwardSegment)
            }

        if (lowerSegment.end().v <= higherSegment.end().v) {
            // lower start, lower end
            finalSegments.add(lowerSegment)
        } else {
            // higher start, lower end
            val intersection = intersection(
                lowerSegment.start,
                higherSegment.start
            )
            finalSegments.add(DisplacementSegment(lowerSegment.start, intersection))
            finalSegments.add(
                DisplacementSegment(
                    higherSegment[intersection],
                    higherSegment.dx - intersection
                )
            )
        }

        i++
    }

    return DisplacementProfile(finalSegments)
}

fun generateSimpleOnlineMotionProfile(
    start: DisplacementState,
    goal: DisplacementState,
    length: Double,
    maxVel: Double,
    maxAccel: Double,
    clock: NanoClock = NanoClock.system()
) = OnlineMotionProfile(
    start,
    goal,
    length,
    object : MotionConstraints() {
        override fun get(s: Double) = SimpleMotionConstraints(maxVel, maxAccel)
        override fun get(s: DoubleProgression) = s.map { get(it) }
    },
    clock
)

private fun forwardPass(
    start: DisplacementState,
    displacements: DoubleProgression,
    constraints: List<SimpleMotionConstraints>
): DisplacementProfile {
    val forwardSegments = mutableListOf<DisplacementSegment>()

    val dx = displacements.step

    var lastState = start
    constraints
        .dropLast(1)
        .forEach { constraint ->
            // compute the segment constraints
            val maxVel = constraint.maxVel
            val maxAccel = constraint.maxAccel

            lastState = if (lastState.v >= maxVel) {
                // the last velocity exceeds max vel so we just coast
                val segment = DisplacementSegment(DisplacementState(maxVel), dx)
                forwardSegments.add(segment)
                segment.end()
            } else {
                // compute the final velocity assuming max accel
                val finalVel = sqrt(lastState.v * lastState.v + 2 * maxAccel * dx)
                if (finalVel <= maxVel) {
                    // we're still under max vel so we're good
                    val segment = DisplacementSegment(DisplacementState(lastState.v, maxAccel), dx)
                    forwardSegments.add(segment)
                    segment.end()
                } else {
                    // we went over max vel so now we split the segment
                    val accelDx = (maxVel * maxVel - lastState.v * lastState.v) / (2 * maxAccel)
                    val accelState = DisplacementState(lastState.v, maxAccel)
                    val coastState = DisplacementState(maxVel)
                    val accelSegment = DisplacementSegment(accelState, accelDx)
                    val coastSegment = DisplacementSegment(coastState, dx - accelDx)
                    forwardSegments.add(accelSegment)
                    forwardSegments.add(coastSegment)
                    coastSegment.end()
                }
            }
        }

    return DisplacementProfile(forwardSegments)
}

private fun intersection(state1: DisplacementState, state2: DisplacementState): Double {
    return (state1.v * state1.v - state2.v * state2.v) / (2 * state2.a - 2 * state1.a)
}
interface TrajectoryConstraints {

    /**
     * Returns the maximum velocity and acceleration for the given path displacement and pose derivatives.
     *
     * @param s path displacement
     * @param pose pose
     * @param deriv pose derivative
     * @param secondDeriv pose second derivative
     */
    operator fun get(s: Double, pose: Pose, deriv: Pose, secondDeriv: Pose): SimpleMotionConstraints
}

open class DriveConstraints(
    @JvmField var maxVel: Double,
    @JvmField var maxAccel: Double,
    @JvmField var maxJerk: Double,
    @JvmField var maxAngVel: Double,
    @JvmField var maxAngAccel: Double,
    @JvmField var maxAngJerk: Double
) : TrajectoryConstraints {
    override fun get(s: Double, pose: Pose, deriv: Pose, secondDeriv: Pose): SimpleMotionConstraints {
        val maxVels = mutableListOf(maxVel)

        if (!(deriv.heading epsilonEquals 0.0)) {
            maxVels.add(maxAngVel / abs(deriv.heading))
        }

        return SimpleMotionConstraints(maxVels.min() ?: 0.0, maxAccel)
    }
}
fun generateConstraints(path: HermitePath, constraints: TrajectoryConstraints) =
    object : MotionConstraints() {
        override fun get(s: Double): SimpleMotionConstraints {
            return constraints[
                    s,
                    path[s],
                    path[s, 1],
                    path[s, 2]
            ]
        }
    }

fun generateOnlineMotionProfile(
    start: DisplacementState,
    goal: DisplacementState,
    length: Double,
    constraints: MotionConstraints,
    clock: NanoClock = NanoClock.system(),
    resolution: Double = 0.25
): OnlineMotionProfile {
    // ds is an adjusted resolution that fits nicely within length
    val samples = ceil(length / resolution).toInt()
    val s = DoubleProgression.fromClosedInterval(0.0, length, samples)

    val constraintsList = constraints[s]
    val lastState = DisplacementState(constraintsList.last().maxVel)

    // we start with last constraint rather than goal because the end ramp is already handled in the online profile
    val backwardProfile = forwardPass(
        lastState,
        s,
        constraintsList.reversed()
    ).reversed()

    return OnlineMotionProfile(start, goal, length, constraints, clock, backwardProfile)
}

fun calculatePoseError(targetFieldPose: Pose, currentFieldPose: Pose) =
    Pose(
        (targetFieldPose.vec - currentFieldPose.vec).rotate(-currentFieldPose.heading),
        (targetFieldPose.heading - currentFieldPose.heading).angleWrap
    )

fun fieldToRobotVelocity(fieldPose: Pose, fieldVel: Pose) =
    Pose(fieldVel.vec.rotate(-fieldPose.heading), fieldVel.heading)

/**
 * Returns the robot pose acceleration corresponding to [fieldPose], [fieldVel], and [fieldAccel].
 */
fun fieldToRobotAcceleration(fieldPose: Pose, fieldVel: Pose, fieldAccel: Pose) =
    Pose(
        fieldAccel.vec.rotate(-fieldPose.heading) + (
                Vector(
                    -fieldVel.x * sin(fieldPose.heading) + fieldVel.y * cos(fieldPose.heading),
                    -fieldVel.x * cos(fieldPose.heading) - fieldVel.y * sin(fieldPose.heading)
                ) * fieldVel.heading),
        fieldAccel.heading
    )
