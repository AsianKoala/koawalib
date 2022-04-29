package com.asiankoala.koawalib.wpilib

import android.R
import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.wpilib.Numbers.N1
import org.checkerframework.checker.units.qual.C
import org.ejml.dense.row.CommonOps_DDRM
import org.ejml.dense.row.MatrixFeatures_DDRM
import org.ejml.dense.row.NormOps_DDRM
import org.ejml.dense.row.factory.DecompositionFactory_DDRM
import org.ejml.simple.SimpleMatrix
import java.util.*

@Suppress("unused")
open class Matrix<R : Num, C : Num>(protected val storage: SimpleMatrix) {
    constructor(rows: Nat<R>, cols: Nat<C>) : this(SimpleMatrix(rows.getNum(), cols.getNum()))
    constructor(other: Matrix<R, C>) : this(other.storage.copy())

    val rows get() = storage.numRows()
    val cols get() = storage.numCols()
    val diag get() = Matrix<R, C>(storage.diag())
    val max get() = CommonOps_DDRM.elementMax(storage.ddrm)
    val maxAbs get() = CommonOps_DDRM.elementMaxAbs(storage.ddrm)
    val min get() = CommonOps_DDRM.elementMin(storage.ddrm)
    val mean: Double get() = elementSum / storage.numElements
    val transpose get() = Matrix<C, R>(storage.transpose())
    val copy get() = Matrix<R, C>(storage.copy())
    val inv get() = Matrix<R, C>(storage.invert())
    val det get() = storage.determinant()
    val normF get() = storage.normF()
    val normIndP1 get() = NormOps_DDRM.inducedP1(storage.ddrm)
    val elementSum get() = storage.elementSum()
    val trace get() = storage.trace()
    val data get() = storage.ddrm.data

    operator fun get(r: Int, c: Int) = storage[r, c]
    operator fun set(r: Int, c: Int, x: Double) {
        storage[r, c] = x
    }

    fun <C2 : Num> times(value: Matrix<C, C2>) = Matrix<R, C>(storage.mult(value.storage))
    open fun times(value: Double): Matrix<R, C> = Matrix(storage.scale(value))
    open operator fun div(value: Double) = Matrix<R, C>(storage.divide(value))
    open operator fun div(value: Int) = Matrix<R, C>(storage.divide(value.d))
    operator fun minus(value: Double) = Matrix<R, C>(storage.minus(value))
    operator fun minus(value: Matrix<R, C>) = Matrix<R, C>(storage.minus(value.storage))
    operator fun plus(value: Double) = Matrix<R, C>(storage.plus(value))
    operator fun plus(value: Matrix<R, C>) = Matrix<R, C>(storage.minus(value.storage))

    fun elementTimes(other: Matrix<R, C>): Matrix<R, C> {
        return Matrix(storage.elementMult(other.storage))
    }

    fun setRow(r: Int, m: Matrix<Numbers.N1, C>) {
        storage.setRow(r, 0, *m.storage.ddrm.data)
    }

    fun setCol(c: Int, m: Matrix<R, Numbers.N1>) {
        storage.setColumn(c, 0, *m.storage.ddrm.data)
    }

    fun fill(value: Double) {
        storage.fill(value)
    }

    fun <C2 : Num> solve(b: Matrix<R, C2>): Matrix<C, C2> {
        return Matrix(storage.solve(b.storage))
    }

//    fun pow(exponent: Double): Matrix<R, C> {
//        if (rows != cols) {
//            throw MatrixDimensionException("Non-square matrices cannot be exponentiated! This matrix is $rows x $cols")
//        }
//        val toReturn: Matrix<R, C> =
//            Matrix(SimpleMatrix(rows, cols))
//        WPIMathJNI.exp(
//            storage.ddrm.data,
//            this.rows,
//            exponent,
//            storage.ddrm.data
//        )
//        return toReturn
//    }

    fun elementPower(b: Double): Matrix<R, C> {
        return Matrix(storage.elementPower(b))
    }

    fun elementPower(b: Int): Matrix<R, C> {
        return Matrix(storage.elementPower(b.toDouble()))
    }

    fun extractRowVector(row: Int): Matrix<N1, C> {
        return Matrix(storage.extractVector(true, row))
    }

    fun extractColumnVector(column: Int): Matrix<R, N1> {
        return Matrix(storage.extractVector(false, column))
    }

    fun <R2 : Num, C2 : Num> block(
        height: Nat<R2>,
        width: Nat<C2>,
        startingRow: Int,
        startingCol: Int
    ): Matrix<R2, C2> {
        return Matrix(
            storage.extractMatrix(
                startingRow,
                startingRow + height.getNum(),
                startingCol,
                startingCol + width.getNum()
            )
        )
    }

    fun <R2 : Num, C2 : Num> block(
        height: Int,
        width: Int,
        startingRow: Int,
        startingCol: Int
    ): Matrix<R2, C2> {
        return Matrix(
            storage.extractMatrix(
                startingRow, startingRow + height, startingCol, startingCol + width
            )
        )
    }

    fun <R2 : Num, C2 : Num> assignBlock(
        startingRow: Int,
        startingCol: Int,
        other: Matrix<R2, C2>
    ) {
        storage.insertIntoThis(
            startingRow, startingCol, other.storage
        )
    }

    /**
     * Extracts a submatrix from the supplied matrix and inserts it in a submatrix in "this". The
     * shape of "this" is used to determine the size of the matrix extracted.
     *
     * @param <R2> Number of rows to extract.
     * @param <C2> Number of columns to extract.
     * @param startingRow The starting row in the supplied matrix to extract the submatrix.
     * @param startingCol The starting column in the supplied matrix to extract the submatrix.
     * @param other The matrix to extract the submatrix from.
     </C2></R2> */
    open fun <R2 : Num, C2 : Num> extractFrom(
        startingRow: Int,
        startingCol: Int,
        other: Matrix<R2, C2>
    ) {
        CommonOps_DDRM.extract(
            other.storage.ddrm, startingRow, startingCol, storage.ddrm
        )
    }

    /**
     * Decompose "this" matrix using Cholesky Decomposition. If the "this" matrix is zeros, it will
     * return the zero matrix.
     *
     * @param lowerTriangular Whether or not we want to decompose to the lower triangular Cholesky
     * matrix.
     * @return The decomposed matrix.
     * @throws RuntimeException if the matrix could not be decomposed(ie. is not positive
     * semidefinite).
     */
    open fun lltDecompose(lowerTriangular: Boolean): Matrix<R, C>? {
        val temp: SimpleMatrix = storage.copy()
        val chol = DecompositionFactory_DDRM.chol(temp.numRows(), lowerTriangular)
        if (!chol.decompose(temp.getMatrix())) {
            // check that the input is not all zeros -- if they are, we special case and return all
            // zeros.
            val matData = temp.ddrm.data
            var isZeros = true
            for (matDatum in matData) {
                isZeros = isZeros and (Math.abs(matDatum) < 1e-6)
            }
            if (isZeros) {
                return Matrix(SimpleMatrix(temp.numRows(), temp.numCols()))
            }
            throw RuntimeException("Cholesky decomposition failed! Input matrix:\n$storage")
        }
        return Matrix(SimpleMatrix.wrap(chol.getT(null)))
    }

    companion object {
        fun <D : Num> eye(dim: Nat<D>): Matrix<D, D> {
            return Matrix(SimpleMatrix.identity(dim.getNum()))
        }

        fun <D : Num> eye(dim: D): Matrix<D, D>? {
            return Matrix(SimpleMatrix.identity(dim.getNum()))
        }

        fun <R : Num, C : Num> mat(rows: Nat<R>, cols: Nat<C>): MatBuilder<R, C> {
            return MatBuilder(rows, cols)
        }

        fun <R : Num, C : Num> changeBoundsUnchecked(
            mat: Matrix<*, *>
        ): Matrix<R, C> {
            return Matrix(mat.storage)
        }
    }

    /**
     * Checks if another [Matrix] is identical to "this" one within a specified tolerance.
     *
     *
     * This will check if each element is in tolerance of the corresponding element from the other
     * [Matrix] or if the elements have the same symbolic meaning. For two elements to have the
     * same symbolic meaning they both must be either Double.NaN, Double.POSITIVE_INFINITY, or
     * Double.NEGATIVE_INFINITY.
     *
     *
     * NOTE:It is recommend to use [Matrix.isEqual] over this method when
     * checking if two matrices are equal as [Matrix.isEqual] will return false
     * if an element is uncountable. This method should only be used when uncountable elements need to
     * compared.
     *
     * @param other The [Matrix] to check against this one.
     * @param tolerance The tolerance to check equality with.
     * @return true if this matrix is identical to the one supplied.
     */
    open fun isIdentical(other: Matrix<*, *>, tolerance: Double): Boolean {
        return MatrixFeatures_DDRM.isIdentical(
            storage.ddrm, other.storage.ddrm, tolerance
        )
    }

    /**
     * Checks if another [Matrix] is equal to "this" within a specified tolerance.
     *
     *
     * This will check if each element is in tolerance of the corresponding element from the other
     * [Matrix].
     *
     *
     * tol  |a<sub>ij</sub> - b<sub>ij</sub>|
     *
     * @param other The [Matrix] to check against this one.
     * @param tolerance The tolerance to check equality with.
     * @return true if this matrix is equal to the one supplied.
     */
    open fun isEqual(other: Matrix<*, *>, tolerance: Double): Boolean {
        return MatrixFeatures_DDRM.isEquals(
            storage.ddrm, other.storage.ddrm, tolerance
        )
    }

    override fun toString(): String {
        return storage.toString()
    }

    /**
     * Checks if an object is equal to this [Matrix].
     *
     *
     * a<sub>ij</sub> == b<sub>ij</sub>
     *
     * @param other The Object to check against this [Matrix].
     * @return true if the object supplied is a [Matrix] and is equal to this matrix.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Matrix<*, *>) {
            return false
        }
        val matrix = other
        return if (MatrixFeatures_DDRM.hasUncountable(matrix.storage.ddrm)) {
            false
        } else MatrixFeatures_DDRM.isEquals(storage.ddrm, matrix.storage.ddrm)
    }

    override fun hashCode(): Int {
        return Objects.hash(storage)
    }
}
