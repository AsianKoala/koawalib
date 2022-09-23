package com.asiankoala.koawalib.util

// note: ported from wpilib
@Suppress("unused")
object Units {
    private const val kInchesPerFoot = 12.0
    private const val kMetersPerInch = 0.0254
    private const val kSecondsPerMinute = 60.0
    private const val kMillisecondsPerSecond = 1000.0
    private const val kKilogramsPerLb = 0.453592
    private const val kKgCmPerNewtonMeter = 10.197162

    fun kgCmToNewtonMeters(kgCm: Double): Double {
        return kgCm / kKgCmPerNewtonMeter
    }

    /**
     * Converts given meters to feet.
     *
     * @param meters The meters to convert to feet.
     * @return Feet converted from meters.
     */
    fun metersToFeet(meters: Double): Double {
        return metersToInches(meters) / kInchesPerFoot
    }

    /**
     * Converts given feet to meters.
     *
     * @param feet The feet to convert to meters.
     * @return Meters converted from feet.
     */
    fun feetToMeters(feet: Double): Double {
        return inchesToMeters(feet * kInchesPerFoot)
    }

    /**
     * Converts given meters to inches.
     *
     * @param meters The meters to convert to inches.
     * @return Inches converted from meters.
     */
    fun metersToInches(meters: Double): Double {
        return meters / kMetersPerInch
    }

    /**
     * Converts given inches to meters.
     *
     * @param inches The inches to convert to meters.
     * @return Meters converted from inches.
     */
    fun inchesToMeters(inches: Double): Double {
        return inches * kMetersPerInch
    }

    /**
     * Converts given degrees to radians.
     *
     * @param degrees The degrees to convert to radians.
     * @return Radians converted from degrees.
     */
    fun degreesToRadians(degrees: Double): Double {
        return Math.toRadians(degrees)
    }

    /**
     * Converts given radians to degrees.
     *
     * @param radians The radians to convert to degrees.
     * @return Degrees converted from radians.
     */
    fun radiansToDegrees(radians: Double): Double {
        return Math.toDegrees(radians)
    }

    /**
     * Converts given radians to rotations.
     *
     * @param radians The radians to convert.
     * @return rotations Converted from radians.
     */
    fun radiansToRotations(radians: Double): Double {
        return radians / (Math.PI * 2)
    }

    /**
     * Converts given degrees to rotations.
     *
     * @param degrees The degrees to convert.
     * @return rotations Converted from degrees.
     */
    fun degreesToRotations(degrees: Double): Double {
        return degrees / 360
    }

    /**
     * Converts given rotations to degrees.
     *
     * @param rotations The rotations to convert.
     * @return degrees Converted from rotations.
     */
    fun rotationsToDegrees(rotations: Double): Double {
        return rotations * 360
    }

    /**
     * Converts given rotations to radians.
     *
     * @param rotations The rotations to convert.
     * @return radians Converted from rotations.
     */
    fun rotationsToRadians(rotations: Double): Double {
        return rotations * 2 * Math.PI
    }

    /**
     * Converts rotations per minute to radians per second.
     *
     * @param rpm The rotations per minute to convert to radians per second.
     * @return Radians per second converted from rotations per minute.
     */
    fun rotationsPerMinuteToRadiansPerSecond(rpm: Double): Double {
        return rpm * Math.PI / (kSecondsPerMinute / 2)
    }

    /**
     * Converts radians per second to rotations per minute.
     *
     * @param radiansPerSecond The radians per second to convert to from rotations per minute.
     * @return Rotations per minute converted from radians per second.
     */
    fun radiansPerSecondToRotationsPerMinute(radiansPerSecond: Double): Double {
        return radiansPerSecond * (kSecondsPerMinute / 2) / Math.PI
    }

    /**
     * Converts given milliseconds to seconds.
     *
     * @param milliseconds The milliseconds to convert to seconds.
     * @return Seconds converted from milliseconds.
     */
    fun millisecondsToSeconds(milliseconds: Double): Double {
        return milliseconds / kMillisecondsPerSecond
    }

    /**
     * Converts given seconds to milliseconds.
     *
     * @param seconds The seconds to convert to milliseconds.
     * @return Milliseconds converted from seconds.
     */
    fun secondsToMilliseconds(seconds: Double): Double {
        return seconds * kMillisecondsPerSecond
    }

    /**
     * Converts kilograms into lbs (pound-mass).
     *
     * @param kilograms The kilograms to convert to lbs (pound-mass).
     * @return Lbs (pound-mass) converted from kilograms.
     */
    fun kilogramsToLbs(kilograms: Double): Double {
        return kilograms / kKilogramsPerLb
    }

    /**
     * Converts lbs (pound-mass) into kilograms.
     *
     * @param lbs The lbs (pound-mass) to convert to kilograms.
     * @return Kilograms converted from lbs (pound-mass).
     */
    fun lbsToKilograms(lbs: Double): Double {
        return lbs * kKilogramsPerLb
    }
}
