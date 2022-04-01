package com.asiankoala.koawalib.roadrunner.trajectorysequence.sequencesegment

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.profile.MotionProfile

import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker
import com.acmerobotics.roadrunner.util.Angle


class TurnSegment(
    startPose: Pose2d,
    val totalRotation: Double,
    private val motionProfile: MotionProfile,
    markers: List<TrajectoryMarker>
) :
    SequenceSegment(
        motionProfile.duration(),
        startPose,
        Pose2d(
            startPose.x, startPose.y,
            Angle.norm(startPose.heading + totalRotation)
        ),
        markers
    ) {
    fun getMotionProfile(): MotionProfile {
        return motionProfile
    }

}
