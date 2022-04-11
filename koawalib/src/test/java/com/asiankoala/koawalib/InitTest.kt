package com.asiankoala.koawalib

object InitTest {
    open class t {
        init {
            println("hi")
        }
    }

    class f : t() {
        init {
            println("meow")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val p = f()
    }
}