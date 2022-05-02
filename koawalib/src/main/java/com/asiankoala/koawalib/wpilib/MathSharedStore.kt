// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package edu.wpi.first.math

object MathSharedStore {
    /**
     * Get the MathShared object.
     *
     * @return The MathShared object.
     */
    /**
     * Set the MathShared object.
     *
     * @param shared The MathShared object.
     */
    @get:Synchronized
    @set:Synchronized
    var mathShared: MathShared? = null
        get() {
            if (field == null) {
                field = object : MathShared {
                    override fun reportError(
                        error: String?,
                        stackTrace: Array<StackTraceElement?>?
                    ) {
                    }

                    override fun reportUsage(id: MathUsageId?, count: Int) {}
                }
            }
            return field
        }

    /**
     * Report an error.
     *
     * @param error the error to set
     * @param stackTrace array of stacktrace elements
     */
    fun reportError(error: String?, stackTrace: Array<StackTraceElement?>?) {
        mathShared!!.reportError(error, stackTrace)
    }

    /**
     * Report usage.
     *
     * @param id the usage id
     * @param count the usage count
     */
    fun reportUsage(id: MathUsageId?, count: Int) {
        mathShared!!.reportUsage(id, count)
    }
}