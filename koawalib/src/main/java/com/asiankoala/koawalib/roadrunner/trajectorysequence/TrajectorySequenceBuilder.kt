package com.asiankoala.koawalib.roadrunner.trajectorysequence


import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.PathContinuityViolationException
import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator.generateSimpleMotionProfile
import com.acmerobotics.roadrunner.profile.MotionState
import com.acmerobotics.roadrunner.trajectory.*
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint
import com.acmerobotics.roadrunner.util.Angle.norm
import com.asiankoala.koawalib.roadrunner.trajectorysequence.sequencesegment.SequenceSegment
import com.asiankoala.koawalib.roadrunner.trajectorysequence.sequencesegment.TrajectorySegment
import com.asiankoala.koawalib.roadrunner.trajectorysequence.sequencesegment.TurnSegment
import com.asiankoala.koawalib.roadrunner.trajectorysequence.sequencesegment.WaitSegment


class TrajectorySequenceBuilder(
    startPose: Pose2d,
    startTangent: Double?,
    private val baseVelConstraint: TrajectoryVelocityConstraint,
    private val baseAccelConstraint: TrajectoryAccelerationConstraint,
    baseTurnConstraintMaxAngVel: Double,
    baseTurnConstraintMaxAngAccel: Double
) {
    private val resolution = 0.25
    private var currentVelConstraint: TrajectoryVelocityConstraint
    private var currentAccelConstraint: TrajectoryAccelerationConstraint
    private val baseTurnConstraintMaxAngVel: Double
    private val baseTurnConstraintMaxAngAccel: Double
    private var currentTurnConstraintMaxAngVel: Double
    private var currentTurnConstraintMaxAngAccel: Double
    private val sequenceSegments: MutableList<SequenceSegment>
    private val temporalMarkers: MutableList<TemporalMarker>
    private val displacementMarkers: MutableList<DisplacementMarker>
    private val spatialMarkers: MutableList<SpatialMarker>
    private var lastPose: Pose2d
    private var tangentOffset: Double
    private var setAbsoluteTangent: Boolean
    private var absoluteTangent: Double
    private var currentTrajectoryBuilder: TrajectoryBuilder? = null
    private var currentDuration: Double
    private var currentDisplacement: Double
    private var lastDurationTraj: Double
    private var lastDisplacementTraj: Double

    constructor(
        startPose: Pose2d,
        baseVelConstraint: TrajectoryVelocityConstraint,
        baseAccelConstraint: TrajectoryAccelerationConstraint,
        baseTurnConstraintMaxAngVel: Double,
        baseTurnConstraintMaxAngAccel: Double
    ) : this(
        startPose, null,
        baseVelConstraint, baseAccelConstraint,
        baseTurnConstraintMaxAngVel, baseTurnConstraintMaxAngAccel
    ) {
    }

    fun lineTo(endPosition: Vector2d): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.lineTo(
                endPosition,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun lineTo(
        endPosition: Vector2d,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.lineTo(
                endPosition,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun lineToConstantHeading(endPosition: Vector2d): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.lineToConstantHeading(
                endPosition,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun lineToConstantHeading(
        endPosition: Vector2d,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.lineToConstantHeading(
                endPosition,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun lineToLinearHeading(endPose: Pose2d): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.lineToLinearHeading(
                endPose,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun lineToLinearHeading(
        endPose: Pose2d,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.lineToLinearHeading(
                endPose,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun lineToSplineHeading(endPose: Pose2d): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.lineToSplineHeading(
                endPose,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun lineToSplineHeading(
        endPose: Pose2d,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.lineToSplineHeading(
                endPose,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun strafeTo(endPosition: Vector2d): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.strafeTo(
                endPosition,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun strafeTo(
        endPosition: Vector2d,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.strafeTo(
                endPosition,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun forward(distance: Double): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.forward(
                distance,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun forward(
        distance: Double,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.forward(
                distance,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun back(distance: Double): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.back(
                distance,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun back(
        distance: Double,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.back(
                distance,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun strafeLeft(distance: Double): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.strafeLeft(
                distance,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun strafeLeft(
        distance: Double,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.strafeLeft(
                distance,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun strafeRight(distance: Double): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.strafeRight(
                distance,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun strafeRight(
        distance: Double,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.strafeRight(
                distance,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun splineTo(endPosition: Vector2d, endHeading: Double): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.splineTo(
                endPosition,
                endHeading,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun splineTo(
        endPosition: Vector2d,
        endHeading: Double,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.splineTo(
                endPosition,
                endHeading,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun splineToConstantHeading(
        endPosition: Vector2d,
        endHeading: Double
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.splineToConstantHeading(
                endPosition,
                endHeading,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun splineToConstantHeading(
        endPosition: Vector2d,
        endHeading: Double,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.splineToConstantHeading(
                endPosition,
                endHeading,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun splineToLinearHeading(endPose: Pose2d, endHeading: Double): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.splineToLinearHeading(
                endPose,
                endHeading,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun splineToLinearHeading(
        endPose: Pose2d,
        endHeading: Double,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.splineToLinearHeading(
                endPose,
                endHeading,
                velConstraint,
                accelConstraint
            )
        })
    }

    fun splineToSplineHeading(endPose: Pose2d, endHeading: Double): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.splineToSplineHeading(
                endPose,
                endHeading,
                currentVelConstraint,
                currentAccelConstraint
            )
        })
    }

    fun splineToSplineHeading(
        endPose: Pose2d,
        endHeading: Double,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        return addPath(AddPathCallback {
            currentTrajectoryBuilder!!.splineToSplineHeading(
                endPose,
                endHeading,
                velConstraint,
                accelConstraint
            )
        })
    }

    private fun addPath(callback: AddPathCallback): TrajectorySequenceBuilder {
        if (currentTrajectoryBuilder == null) newPath()
        try {
            callback.run()
        } catch (e: PathContinuityViolationException) {
            newPath()
            callback.run()
        }
        val builtTraj = currentTrajectoryBuilder!!.build()
        val durationDifference = builtTraj.duration() - lastDurationTraj
        val displacementDifference = builtTraj.path.length() - lastDisplacementTraj
        lastPose = builtTraj.end()
        currentDuration += durationDifference
        currentDisplacement += displacementDifference
        lastDurationTraj = builtTraj.duration()
        lastDisplacementTraj = builtTraj.path.length()
        return this
    }

    fun setTangent(tangent: Double): TrajectorySequenceBuilder {
        setAbsoluteTangent = true
        absoluteTangent = tangent
        pushPath()
        return this
    }

    private fun setTangentOffset(offset: Double): TrajectorySequenceBuilder {
        setAbsoluteTangent = false
        tangentOffset = offset
        pushPath()
        return this
    }

    fun setReversed(reversed: Boolean): TrajectorySequenceBuilder {
        return if (reversed) setTangentOffset(Math.toRadians(180.0)) else setTangentOffset(0.0)
    }

    fun setConstraints(
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        currentVelConstraint = velConstraint
        currentAccelConstraint = accelConstraint
        return this
    }

    fun resetConstraints(): TrajectorySequenceBuilder {
        currentVelConstraint = baseVelConstraint
        currentAccelConstraint = baseAccelConstraint
        return this
    }

    fun setVelConstraint(velConstraint: TrajectoryVelocityConstraint): TrajectorySequenceBuilder {
        currentVelConstraint = velConstraint
        return this
    }

    fun resetVelConstraint(): TrajectorySequenceBuilder {
        currentVelConstraint = baseVelConstraint
        return this
    }

    fun setAccelConstraint(accelConstraint: TrajectoryAccelerationConstraint): TrajectorySequenceBuilder {
        currentAccelConstraint = accelConstraint
        return this
    }

    fun resetAccelConstraint(): TrajectorySequenceBuilder {
        currentAccelConstraint = baseAccelConstraint
        return this
    }

    fun setTurnConstraint(maxAngVel: Double, maxAngAccel: Double): TrajectorySequenceBuilder {
        currentTurnConstraintMaxAngVel = maxAngVel
        currentTurnConstraintMaxAngAccel = maxAngAccel
        return this
    }

    fun resetTurnConstraint(): TrajectorySequenceBuilder {
        currentTurnConstraintMaxAngVel = baseTurnConstraintMaxAngVel
        currentTurnConstraintMaxAngAccel = baseTurnConstraintMaxAngAccel
        return this
    }

    fun addTemporalMarker(callback: MarkerCallback): TrajectorySequenceBuilder {
        return this.addTemporalMarker(currentDuration, callback)
    }

    fun UNSTABLE_addTemporalMarkerOffset(
        offset: Double,
        callback: MarkerCallback
    ): TrajectorySequenceBuilder {
        return this.addTemporalMarker(currentDuration + offset, callback)
    }

    fun addTemporalMarker(time: Double, callback: MarkerCallback): TrajectorySequenceBuilder {
        return this.addTemporalMarker(0.0, time, callback)
    }

    fun addTemporalMarker(
        scale: Double,
        offset: Double,
        callback: MarkerCallback
    ): TrajectorySequenceBuilder {
        return this.addTemporalMarker({ time: Double -> scale * time + offset }, callback)
    }

    fun addTemporalMarker(
        time: TimeProducer,
        callback: MarkerCallback
    ): TrajectorySequenceBuilder {
        temporalMarkers.add(TemporalMarker(time, callback))
        return this
    }

    fun addSpatialMarker(point: Vector2d, callback: MarkerCallback): TrajectorySequenceBuilder {
        spatialMarkers.add(SpatialMarker(point, callback))
        return this
    }

    fun addDisplacementMarker(callback: MarkerCallback): TrajectorySequenceBuilder {
        return this.addDisplacementMarker(currentDisplacement, callback)
    }

    fun UNSTABLE_addDisplacementMarkerOffset(
        offset: Double,
        callback: MarkerCallback
    ): TrajectorySequenceBuilder {
        return this.addDisplacementMarker(currentDisplacement + offset, callback)
    }

    fun addDisplacementMarker(
        displacement: Double,
        callback: MarkerCallback
    ): TrajectorySequenceBuilder {
        return this.addDisplacementMarker(0.0, displacement, callback)
    }

    fun addDisplacementMarker(
        scale: Double,
        offset: Double,
        callback: MarkerCallback
    ): TrajectorySequenceBuilder {
        return addDisplacementMarker(
            { displacement: Double -> scale * displacement + offset },
            callback
        )
    }

    fun addDisplacementMarker(
        displacement: DisplacementProducer,
        callback: MarkerCallback
    ): TrajectorySequenceBuilder {
        displacementMarkers.add(DisplacementMarker(displacement, callback))
        return this
    }

    @JvmOverloads
    fun turn(
        angle: Double,
        maxAngVel: Double = currentTurnConstraintMaxAngVel,
        maxAngAccel: Double = currentTurnConstraintMaxAngAccel
    ): TrajectorySequenceBuilder {
        pushPath()
        val turnProfile = generateSimpleMotionProfile(
            MotionState(lastPose.heading, 0.0, 0.0, 0.0),
            MotionState(lastPose.heading + angle, 0.0, 0.0, 0.0),
            maxAngVel,
            maxAngAccel
        )
        sequenceSegments.add(TurnSegment(lastPose, angle, turnProfile, emptyList()))
        lastPose = Pose2d(
            lastPose.x, lastPose.y,
            norm(lastPose.heading + angle)
        )
        currentDuration += turnProfile.duration()
        return this
    }

    fun waitSeconds(seconds: Double): TrajectorySequenceBuilder {
        pushPath()
        sequenceSegments.add(WaitSegment(lastPose, seconds, emptyList()))
        currentDuration += seconds
        return this
    }

    fun addTrajectory(trajectory: Trajectory): TrajectorySequenceBuilder {
        pushPath()
        sequenceSegments.add(TrajectorySegment(trajectory))
        return this
    }

    private fun pushPath() {
        if (currentTrajectoryBuilder != null) {
            val builtTraj = currentTrajectoryBuilder!!.build()
            sequenceSegments.add(TrajectorySegment(builtTraj))
        }
        currentTrajectoryBuilder = null
    }

    private fun newPath() {
        if (currentTrajectoryBuilder != null) pushPath()
        lastDurationTraj = 0.0
        lastDisplacementTraj = 0.0
        val tangent =
            if (setAbsoluteTangent) absoluteTangent else norm(lastPose.heading + tangentOffset)
        currentTrajectoryBuilder = TrajectoryBuilder(
            lastPose, tangent,
            currentVelConstraint, currentAccelConstraint, resolution
        )
    }

    fun build(): TrajectorySequence {
        pushPath()
        val globalMarkers = convertMarkersToGlobal(
            sequenceSegments.toList(),
            temporalMarkers, displacementMarkers, spatialMarkers
        )
        return TrajectorySequence(
            projectGlobalMarkersToLocalSegments(
                globalMarkers,
                sequenceSegments
            )
        )
    }

    private fun convertMarkersToGlobal(
        sequenceSegments: List<SequenceSegment>,
        temporalMarkers: List<TemporalMarker>,
        displacementMarkers: List<DisplacementMarker>,
        spatialMarkers: List<SpatialMarker>
    ): List<TrajectoryMarker> {
        val trajectoryMarkers = ArrayList<TrajectoryMarker>()

        // Convert temporal markers
        for ((producer, callback) in temporalMarkers) {
            trajectoryMarkers.add(
                TrajectoryMarker(producer.produce(currentDuration), callback)
            )
        }

        // Convert displacement markers
        for ((producer, callback) in displacementMarkers) {
            val time = displacementToTime(
                sequenceSegments,
                producer.produce(currentDisplacement)
            )
            trajectoryMarkers.add(
                TrajectoryMarker(
                    time,
                    callback
                )
            )
        }

        // Convert spatial markers
        for ((point, callback) in spatialMarkers) {
            trajectoryMarkers.add(
                TrajectoryMarker(
                    pointToTime(sequenceSegments, point),
                    callback
                )
            )
        }
        return trajectoryMarkers
    }

    private fun projectGlobalMarkersToLocalSegments(
        markers: List<TrajectoryMarker>,
        sequenceSegments: MutableList<SequenceSegment>
    ): List<SequenceSegment> {
        if (sequenceSegments.isEmpty()) return emptyList()
        var totalSequenceDuration = 0.0
        for (segment in sequenceSegments) {
            totalSequenceDuration += segment!!.duration
        }
        for ((time, callback) in markers) {
            var segment: SequenceSegment? = null
            var segmentIndex = 0
            var segmentOffsetTime = 0.0
            var currentTime = 0.0
            for (i in sequenceSegments.indices) {
                val seg = sequenceSegments[i]
                val markerTime = Math.min(time, totalSequenceDuration)
                if (currentTime + seg!!.duration >= markerTime) {
                    segment = seg
                    segmentIndex = i
                    segmentOffsetTime = markerTime - currentTime
                    break
                } else {
                    currentTime += seg.duration
                }
            }
            var newSegment: SequenceSegment? = null
            if (segment is WaitSegment) {
                val newMarkers: MutableList<TrajectoryMarker> = ArrayList(segment.markers)
                newMarkers.addAll(sequenceSegments[segmentIndex].markers)
                newMarkers.add(TrajectoryMarker(segmentOffsetTime, callback))
                val thisSegment = segment
                newSegment = WaitSegment(thisSegment.startPose, thisSegment.duration, newMarkers)
            } else if (segment is TurnSegment) {
                val newMarkers: MutableList<TrajectoryMarker> = ArrayList(segment.markers)
                newMarkers.addAll(sequenceSegments[segmentIndex]!!.markers)
                newMarkers.add(TrajectoryMarker(segmentOffsetTime, callback))
                val thisSegment = segment
                newSegment = TurnSegment(
                    thisSegment.startPose,
                    thisSegment.totalRotation,
                    thisSegment.getMotionProfile(),
                    newMarkers
                )
            } else if (segment is TrajectorySegment) {
                val thisSegment = segment
                val newMarkers: MutableList<TrajectoryMarker> =
                    ArrayList(thisSegment.trajectory.markers)
                newMarkers.add(TrajectoryMarker(segmentOffsetTime, callback))
                newSegment = TrajectorySegment(
                    Trajectory(
                        thisSegment.trajectory.path,
                        thisSegment.trajectory.profile,
                        newMarkers
                    )
                )
            }
            sequenceSegments[segmentIndex] = newSegment!!
        }
        return sequenceSegments
    }

    // Taken from Road Runner's TrajectoryGenerator.displacementToTime() since it's private
    // note: this assumes that the profile position is monotonic increasing
    private fun motionProfileDisplacementToTime(profile: MotionProfile, s: Double): Double {
        var tLo = 0.0
        var tHi = profile.duration()
        while (Math.abs(tLo - tHi) >= 1e-6) {
            val tMid = 0.5 * (tLo + tHi)
            if (profile[tMid].x > s) {
                tHi = tMid
            } else {
                tLo = tMid
            }
        }
        return 0.5 * (tLo + tHi)
    }

    private fun displacementToTime(sequenceSegments: List<SequenceSegment>, s: Double): Double {
        var currentTime = 0.0
        var currentDisplacement = 0.0
        for (segment in sequenceSegments) {
            if (segment is TrajectorySegment) {
                val thisSegment = segment
                val segmentLength = thisSegment.trajectory.path.length()
                if (currentDisplacement + segmentLength > s) {
                    val target = s - currentDisplacement
                    val timeInSegment = motionProfileDisplacementToTime(
                        thisSegment.trajectory.profile,
                        target
                    )
                    return currentTime + timeInSegment
                } else {
                    currentDisplacement += segmentLength
                    currentTime += thisSegment.trajectory.duration()
                }
            } else {
                currentTime += segment.duration
            }
        }
        return 0.0
    }

    private fun pointToTime(sequenceSegments: List<SequenceSegment>, point: Vector2d): Double {
        class ComparingPoints(
            val distanceToPoint: Double,
            val totalDisplacement: Double,
            val thisPathDisplacement: Double
        )

        val projectedPoints: MutableList<ComparingPoints> = ArrayList()
        for (segment in sequenceSegments) {
            if (segment is TrajectorySegment) {
                val thisSegment = segment
                val displacement = thisSegment.trajectory.path.project(point, 0.25)
                val projectedPoint = thisSegment.trajectory.path[displacement].vec()
                val distanceToPoint = point.minus(projectedPoint).norm()
                var totalDisplacement = 0.0
                for (comparingPoint in projectedPoints) {
                    totalDisplacement += comparingPoint.totalDisplacement
                }
                totalDisplacement += displacement
                projectedPoints.add(
                    ComparingPoints(
                        distanceToPoint,
                        displacement,
                        totalDisplacement
                    )
                )
            }
        }
        var closestPoint: ComparingPoints? = null
        for (comparingPoint in projectedPoints) {
            if (closestPoint == null) {
                closestPoint = comparingPoint
                continue
            }
            if (comparingPoint.distanceToPoint < closestPoint.distanceToPoint) closestPoint =
                comparingPoint
        }
        return displacementToTime(sequenceSegments, closestPoint!!.thisPathDisplacement)
    }

    private fun interface AddPathCallback {
        fun run()
    }

    init {
        currentVelConstraint = baseVelConstraint
        currentAccelConstraint = baseAccelConstraint
        this.baseTurnConstraintMaxAngVel = baseTurnConstraintMaxAngVel
        this.baseTurnConstraintMaxAngAccel = baseTurnConstraintMaxAngAccel
        currentTurnConstraintMaxAngVel = baseTurnConstraintMaxAngVel
        currentTurnConstraintMaxAngAccel = baseTurnConstraintMaxAngAccel
        sequenceSegments = ArrayList()
        temporalMarkers = ArrayList()
        displacementMarkers = ArrayList()
        spatialMarkers = ArrayList()
        lastPose = startPose
        tangentOffset = 0.0
        setAbsoluteTangent = startTangent != null
        absoluteTangent = startTangent ?: 0.0
        currentTrajectoryBuilder = null
        currentDuration = 0.0
        currentDisplacement = 0.0
        lastDurationTraj = 0.0
        lastDisplacementTraj = 0.0
    }
}