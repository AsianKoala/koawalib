package com.asiankoala.koawalib.roadrunner.trajectorysequence.sequencesegment

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker


class WaitSegment(pose: Pose2d, seconds: Double, markers: List<TrajectoryMarker>) :
    SequenceSegment(seconds, pose, pose, markers)
