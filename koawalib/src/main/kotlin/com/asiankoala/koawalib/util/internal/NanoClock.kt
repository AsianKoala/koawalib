package com.asiankoala.koawalib.util.internal

internal abstract class NanoClock {

    companion object {
        /**
         * Returns a [NanoClock] backed by [System.nanoTime].
         */
        @JvmStatic
        fun system() = object : NanoClock() {
            override fun seconds() = System.nanoTime() / 1e9
            override fun milliseconds() = System.nanoTime() / 1e6
        }
    }

    /**
     * Returns the number of seconds since an arbitrary (yet consistent) origin.
     */
    abstract fun seconds(): Double
    abstract fun milliseconds(): Double
}
