package com.asiankoala.koawalib.wpilib

@Suppress("unused")
fun interface Nat<T : Num> {
    fun getNum(): Int

    companion object {
        fun N0(): Nat<Numbers.N0> {
            return Nat { 0 }
        }

        fun N1(): Nat<Numbers.N1> {
            return Nat { 1 }
        }

        fun N2(): Nat<Numbers.N2> {
            return Nat { 2 }
        }

        fun N3(): Nat<Numbers.N3> {
            return Nat { 3 }
        }

        fun N4(): Nat<Numbers.N4> {
            return Nat { 4 }
        }

        fun N5(): Nat<Numbers.N5> {
            return Nat { 5 }
        }

        fun N6(): Nat<Numbers.N6> {
            return Nat { 6 }
        }

        fun N7(): Nat<Numbers.N7> {
            return Nat { 7 }
        }

        fun N8(): Nat<Numbers.N8> {
            return Nat { 8 }
        }

        fun N9(): Nat<Numbers.N9> {
            return Nat { 9 }
        }

        fun N10(): Nat<Numbers.N10> {
            return Nat { 10 }
        }

        fun N11(): Nat<Numbers.N11> {
            return Nat { 11 }
        }

        fun N12(): Nat<Numbers.N12> {
            return Nat { 12 }
        }

        fun N13(): Nat<Numbers.N13> {
            return Nat { 13 }
        }

        fun N14(): Nat<Numbers.N14> {
            return Nat { 14 }
        }

        fun N15(): Nat<Numbers.N15> {
            return Nat { 15 }
        }

        fun N16(): Nat<Numbers.N16> {
            return Nat { 16 }
        }

        fun N17(): Nat<Numbers.N17> {
            return Nat { 17 }
        }

        fun N18(): Nat<Numbers.N18> {
            return Nat { 18 }
        }

        fun N19(): Nat<Numbers.N19> {
            return Nat { 19 }
        }

        fun N20(): Nat<Numbers.N20> {
            return Nat { 20 }
        }
    }
}
