package com.asiankoala.koawalib.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.asiankoala.koawalib.roadrunner.trajectorysequence.sequencesegment.SequenceSegment
import java.util.*


class TrajectorySequence(sequenceList: List<SequenceSegment>) {
    private val sequenceList: List<SequenceSegment>
    fun start(): Pose2d {
        return sequenceList[0].startPose
    }

    fun end(): Pose2d {
        return sequenceList[sequenceList.size - 1].endPose
    }

    fun duration(): Double {
        var total = 0.0
        for (segment in sequenceList) {
            total += segment.duration
        }
        return total
    }

    operator fun get(i: Int): SequenceSegment {
        return sequenceList[i]
    }

    fun size(): Int {
        return sequenceList.size
    }

    init {
        if (sequenceList.isEmpty()) throw EmptySequenceException()
        this.sequenceList = Collections.unmodifiableList(sequenceList)
    }
}
