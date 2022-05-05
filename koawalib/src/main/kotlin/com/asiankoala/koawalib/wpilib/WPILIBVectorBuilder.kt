package com.asiankoala.koawalib.wpilib

class WPILIBVectorBuilder<N : Num>(rows: Nat<N>) : MatBuilder<N, Numbers.N1>(rows, Nat.N1()) {
    private fun fillVec(vararg data: Double): WPILIBVector<N> {
        return WPILIBVector(fill(*data))
    }

    companion object {
        fun fill(n1: Double): WPILIBVector<Numbers.N1> {
            return WPILIBVectorBuilder(Nat.N1()).fillVec(n1)
        }

        fun fill(n1: Double, n2: Double): WPILIBVector<Numbers.N2> {
            return WPILIBVectorBuilder(Nat.N2()).fillVec(n1, n2)
        }

        fun fill(n1: Double, n2: Double, n3: Double): WPILIBVector<Numbers.N3> {
            return WPILIBVectorBuilder(Nat.N3()).fillVec(n1, n2, n3)
        }

        fun fill(n1: Double, n2: Double, n3: Double, n4: Double): WPILIBVector<Numbers.N4> {
            return WPILIBVectorBuilder(Nat.N4()).fillVec(n1, n2, n3, n4)
        }

        fun fill(n1: Double, n2: Double, n3: Double, n4: Double, n5: Double): WPILIBVector<Numbers.N5> {
            return WPILIBVectorBuilder(Nat.N5()).fillVec(n1, n2, n3, n4, n5)
        }

        fun fill(
            n1: Double,
            n2: Double,
            n3: Double,
            n4: Double,
            n5: Double,
            n6: Double
        ): WPILIBVector<Numbers.N6> {
            return WPILIBVectorBuilder(Nat.N6()).fillVec(n1, n2, n3, n4, n5, n6)
        }

        fun fill(
            n1: Double,
            n2: Double,
            n3: Double,
            n4: Double,
            n5: Double,
            n6: Double,
            n7: Double
        ): WPILIBVector<Numbers.N7> {
            return WPILIBVectorBuilder(Nat.N7()).fillVec(n1, n2, n3, n4, n5, n6, n7)
        }

        fun fill(
            n1: Double,
            n2: Double,
            n3: Double,
            n4: Double,
            n5: Double,
            n6: Double,
            n7: Double,
            n8: Double
        ): WPILIBVector<Numbers.N8> {
            return WPILIBVectorBuilder(Nat.N8()).fillVec(n1, n2, n3, n4, n5, n6, n7, n8)
        }

        fun fill(
            n1: Double,
            n2: Double,
            n3: Double,
            n4: Double,
            n5: Double,
            n6: Double,
            n7: Double,
            n8: Double,
            n9: Double
        ): WPILIBVector<Numbers.N9> {
            return WPILIBVectorBuilder(Nat.N9()).fillVec(n1, n2, n3, n4, n5, n6, n7, n8, n9)
        }

        fun fill(
            n1: Double,
            n2: Double,
            n3: Double,
            n4: Double,
            n5: Double,
            n6: Double,
            n7: Double,
            n8: Double,
            n9: Double,
            n10: Double
        ): WPILIBVector<Numbers.N10> {
            return WPILIBVectorBuilder(Nat.N10()).fillVec(n1, n2, n3, n4, n5, n6, n7, n8, n9, n10)
        }
    }
}
