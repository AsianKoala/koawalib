// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package edu.wpi.first.math

interface MathShared {
    /**
     * Report an error.
     *
     * @param error the error to set
     * @param stackTrace array of stacktrace elements
     */
    fun reportError(error: String?, stackTrace: Array<StackTraceElement?>?)

    /**
     * Report usage.
     *
     * @param id the usage id
     * @param count the usage count
     */
    fun reportUsage(id: MathUsageId?, count: Int)
}